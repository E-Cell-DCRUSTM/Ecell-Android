import { initializeApp } from "firebase-admin/app";
import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { buildContactMigrationPlan, parseMigrationArguments } from "./contactMigration";

async function main(): Promise<void> {
  const options = parseMigrationArguments(process.argv.slice(2), process.env.GCLOUD_PROJECT);
  if (options.help) {
    console.log("Usage: npm run migrate:contacts -- --project-id <id> [--dry-run|--apply]");
    console.log("Dry-run is the default. --apply is required before any Firestore writes.");
    return;
  }

  initializeApp({ projectId: options.projectId });
  const db = getFirestore();
  const [usersSnapshot, contactsSnapshot, devicesSnapshot] = await Promise.all([
    db.collection("users").get(),
    db.collection("userContacts").get(),
    db.collection("userDevices").get(),
  ]);

  const plan = buildContactMigrationPlan(
    usersSnapshot.docs.map((doc) => ({ id: doc.id, data: doc.data() })),
    contactsSnapshot.docs.map((doc) => ({ id: doc.id, data: doc.data() })),
    devicesSnapshot.docs.map((doc) => ({ id: doc.id, data: doc.data() })),
  );

  if (plan.issues.length > 0) {
    console.error(`Preflight failed: ${plan.issues.length} validation issue(s); no writes were made.`);
    for (const issue of plan.issues) {
      console.error(`- UID …${issue.uid.slice(-4)}: ${issue.field} (${issue.message})`);
    }
    process.exitCode = 1;
    return;
  }

  const contactUpdates = plan.records.length;
  const deviceUpdates = plan.records.filter((record) => record.fcmTokens.length > 0).length;
  const legacyProfiles = plan.records.filter((record) => record.removeLegacyFields).length;
  const summary = `profiles=${plan.records.length}, contacts=${contactUpdates}, devices=${deviceUpdates}, legacyProfiles=${legacyProfiles}`;

  if (!options.apply) {
    console.log(`Dry-run for ${options.projectId}: ${summary}. No Firestore writes were made.`);
    console.log("Review the summary, then rerun with --apply to perform the migration.");
    return;
  }

  // Three writes per profile at most; 150 profiles keeps every batch under
  // Firestore's 500-write limit, including the legacy-profile cleanup.
  for (let offset = 0; offset < plan.records.length; offset += 150) {
    const batch = db.batch();
    for (const record of plan.records.slice(offset, offset + 150)) {
      const userRef = db.collection("users").doc(record.uid);
      const contactRef = db.collection("userContacts").doc(record.uid);
      const deviceRef = db.collection("userDevices").doc(record.uid);

      batch.set(contactRef, record.contact, { merge: true });
      if (record.fcmTokens.length > 0) {
        batch.set(deviceRef, { fcmTokens: record.fcmTokens }, { merge: true });
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

  console.log(`Migration applied to ${options.projectId}: ${summary}.`);
}

main().catch((error: unknown) => {
  console.error("Contact migration failed before completion.", error);
  process.exitCode = 1;
});
