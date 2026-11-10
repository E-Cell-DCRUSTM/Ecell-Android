import { initializeApp } from "firebase-admin/app";
import { FieldValue, getFirestore } from "firebase-admin/firestore";
import {
  MigrationBatchError,
  parseMigrationArguments,
  runContactMigration,
  type ContactMigrationRecord,
  type MigrationDocument,
} from "./contactMigration";

function redactedUid(uid: string): string {
  return uid.length > 4 ? `…${uid.slice(-4)}` : "…";
}

function printReport(
  projectId: string,
  apply: boolean,
  result: Awaited<ReturnType<typeof runContactMigration>>,
): void {
  const { report, plan } = result;
  const mode = apply ? "apply preflight" : "dry-run";
  console.log(
    `${mode} for ${projectId}: profiles=${report.totalProfiles}, valid=${report.validProfiles}, ` +
    `invalid=${report.invalidProfiles}, skipped orphan documents=${report.skippedOrphans}, ` +
    `contacts=${report.contactsToWrite}, devices=${report.devicesToWrite}, ` +
    `profiles to clean=${report.profilesToClean}, unique tokens=${report.uniqueTokensToMove}, ` +
    `batches=${report.plannedBatches}.`,
  );

  if (report.emptyDataset) {
    console.log("The source dataset is empty; no Firestore writes were made.");
  }
  for (const record of plan.records) {
    const issueCount = plan.issues.filter((issue) => issue.uid === record.uid).length;
    const state = issueCount > 0 ? `invalid (${issueCount} issue(s))` : "valid";
    console.log(
      `- user ${redactedUid(record.uid)}: ${state}; ` +
      `contact=ready, device=${record.writeDeviceDocument ? `${record.fcmTokens.length} token(s)` : "unchanged"}, ` +
      `legacy cleanup=${record.removeLegacyFields ? "yes" : "no"}`,
    );
  }
  for (const skipped of plan.skipped) {
    console.log(
      `- orphan ${redactedUid(skipped.uid)}: skipped ${skipped.collections.join(" and ")} document(s); no profile exists`,
    );
  }
  for (const issue of plan.issues) {
    console.error(`- user ${redactedUid(issue.uid)}: ${issue.field} — ${issue.message}`);
  }

  if (result.status === "preflight-failed") {
    console.error("Preflight failed; no writes were made. Resolve every reported issue and rerun the dry-run.");
  } else if (result.status === "dry-run") {
    console.log("No Firestore writes were made. Review the full report before using --apply.");
  } else {
    console.log(
      `Migration applied: ${result.migratedProfiles} profile(s) across ${result.completedBatches} batch(es).`,
    );
  }
}

async function writeMigrationBatch(
  firestore: FirebaseFirestore.Firestore,
  records: ContactMigrationRecord[],
): Promise<void> {
  const batch = firestore.batch();
  for (const record of records) {
    const userRef = firestore.collection("users").doc(record.uid);
    const contactRef = firestore.collection("userContacts").doc(record.uid);
    const deviceRef = firestore.collection("userDevices").doc(record.uid);

    // Replace validated docs so no legacy or unknown keys survive the migration.
    batch.set(contactRef, record.contact);
    if (record.writeDeviceDocument) {
      batch.set(deviceRef, { fcmTokens: record.fcmTokens });
    }
    if (record.removeLegacyFields) {
      batch.update(userRef, {
        email: FieldValue.delete(),
        phone: FieldValue.delete(),
        fcmTokens: FieldValue.delete(),
      });
    }
  }
  await batch.commit();
}

async function main(): Promise<void> {
  const options = parseMigrationArguments(process.argv.slice(2), process.env.GCLOUD_PROJECT);
  if (options.help) {
    console.log("Usage: npm run migrate:contacts -- --project-id <id> [--dry-run|--apply]");
    console.log("Dry-run is the default. --apply is required before any Firestore writes.");
    return;
  }

  initializeApp({ projectId: options.projectId });
  const firestore = getFirestore();
  // Read every source collection before planning or writing. The preflight result
  // is global: a single invalid record prevents all batches from starting.
  const [usersSnapshot, contactsSnapshot, devicesSnapshot] = await Promise.all([
    firestore.collection("users").get(),
    firestore.collection("userContacts").get(),
    firestore.collection("userDevices").get(),
  ]);
  const documents = (snapshot: FirebaseFirestore.QuerySnapshot): MigrationDocument[] =>
    snapshot.docs.map((doc) => ({ id: doc.id, data: doc.data() }));

  const result = await runContactMigration(
    documents(usersSnapshot),
    documents(contactsSnapshot),
    documents(devicesSnapshot),
    { apply: options.apply },
    (records) => writeMigrationBatch(firestore, records),
  );
  printReport(options.projectId, options.apply, result);
  if (result.status === "preflight-failed") process.exitCode = 1;
}

main().catch((error: unknown) => {
  if (error instanceof MigrationBatchError) {
    console.error(
      `Migration stopped at batch ${error.failedBatchNumber}/${error.totalBatches}. ` +
      `${error.completedBatches} batch(es), ${error.migratedProfiles} profile(s) completed; ` +
      `no later batches were attempted. Rerun the dry-run before retrying --apply.`,
    );
    console.error(error.cause instanceof Error ? error.cause.message : "Batch commit failed.");
  } else {
    console.error("Contact migration failed before writes could safely complete.", error);
  }
  process.exitCode = 1;
});
