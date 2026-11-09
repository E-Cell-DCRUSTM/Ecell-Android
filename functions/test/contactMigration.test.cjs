const assert = require("node:assert/strict");
const test = require("node:test");
const {
  buildContactMigrationPlan,
  buildContactMigrationReport,
  MigrationBatchError,
  parseMigrationArguments,
  runContactMigration,
} = require("../lib/contactMigration.js");

function user(id, overrides = {}) {
  return {
    id,
    data: {
      uid: id,
      name: `Member ${id}`,
      year: 2,
      branch: "CSE",
      committee: "EVENTS",
      role: "MEMBER",
      bio: "",
      profilePhotoUrl: "",
      joinedAt: 100,
      isApproved: true,
      ...overrides,
    },
  };
}

test("prefers existing contact values and merges unique device tokens", () => {
  const result = buildContactMigrationPlan(
    [user("user-1", { email: "legacy@example.com", phone: "111", fcmTokens: ["old", "shared"] })],
    [{ id: "user-1", data: { email: "current@example.com", phone: "222", committee: "EVENTS" } }],
    [{ id: "user-1", data: { fcmTokens: ["shared", "new"] } }],
  );

  assert.deepEqual(result.issues, []);
  assert.deepEqual(result.records[0], {
    uid: "user-1",
    contact: { email: "current@example.com", phone: "222", committee: "EVENTS" },
    fcmTokens: ["shared", "new", "old"],
    writeDeviceDocument: true,
    removeLegacyFields: true,
  });
});

test("falls back to legacy contacts when existing contact values are blank", () => {
  const result = buildContactMigrationPlan(
    [user("user-2", { email: "legacy@example.com", phone: "123" })],
    [{ id: "user-2", data: { email: "", phone: "", committee: "EVENTS" } }],
    [],
  );

  assert.deepEqual(result.issues, []);
  assert.equal(result.records[0].contact.email, "legacy@example.com");
  assert.equal(result.records[0].contact.phone, "123");
});

test("accepts exactly twenty unique valid FCM tokens", () => {
  const tokens = Array.from({ length: 20 }, (_, index) => `token-${index}`);
  const result = buildContactMigrationPlan([user("user-20")], [], [
    { id: "user-20", data: { fcmTokens: tokens } },
  ]);

  assert.deepEqual(result.issues, []);
  assert.equal(result.records[0].fcmTokens.length, 20);
});

test("deduplicates before enforcing the twenty-token limit", () => {
  const tokens = Array.from({ length: 20 }, (_, index) => `token-${index}`);
  const result = buildContactMigrationPlan(
    [user("user-duplicates", { fcmTokens: [tokens[0], tokens[1]] })],
    [],
    [{ id: "user-duplicates", data: { fcmTokens: tokens } }],
  );

  assert.deepEqual(result.issues, []);
  assert.equal(result.records[0].fcmTokens.length, 20);
});

for (const count of [21, 25]) {
  test(`rejects ${count} unique FCM tokens without trimming`, () => {
    const tokens = Array.from({ length: count }, (_, index) => `token-${index}`);
    const result = buildContactMigrationPlan([user(`user-${count}`)], [], [
      { id: `user-${count}`, data: { fcmTokens: tokens } },
    ]);

    assert.equal(result.records[0].fcmTokens.length, count);
    assert.deepEqual(result.issues, [{
      uid: `user-${count}`,
      field: "userDevices.fcmTokens",
      message: `has ${count} unique tokens; the maximum is 20`,
    }]);
  });
}

test("rejects missing and additional public profile fields", () => {
  const missing = user("missing");
  delete missing.data.branch;
  const result = buildContactMigrationPlan([
    missing,
    user("extra", { unexpected: "must not survive migration" }),
  ], [], []);

  assert.ok(result.issues.some((issue) => issue.uid === "missing" && issue.field === "branch"));
  assert.ok(result.issues.some((issue) => issue.uid === "extra" && issue.field === "unexpected"));
});

test("rejects invalid profile values and inconsistent approval assignments", () => {
  const result = buildContactMigrationPlan([
    user("invalid-values", { year: 9, role: "ROOT", committee: "UNKNOWN" }),
    user("pending-mismatch", { isApproved: false, role: "MEMBER", committee: "EVENTS" }),
  ], [], []);

  assert.ok(result.issues.some((issue) => issue.uid === "invalid-values" && issue.field === "year"));
  assert.ok(result.issues.some((issue) => issue.uid === "invalid-values" && issue.field === "role"));
  assert.ok(result.issues.some((issue) => issue.uid === "invalid-values" && issue.field === "committee"));
  assert.ok(result.issues.some((issue) => issue.uid === "pending-mismatch" && issue.field === "role/committee/isApproved"));
});

test("rejects wrong field types and document ID mismatches", () => {
  const result = buildContactMigrationPlan([
    user("wrong-types", { uid: 42, year: "2", joinedAt: "100", isApproved: 1 }),
  ], [], []);

  for (const field of ["uid", "year", "joinedAt", "isApproved"]) {
    assert.ok(result.issues.some((issue) => issue.field === field), `expected ${field} to be rejected`);
  }
});

test("validates existing contact and device document schemas", () => {
  const result = buildContactMigrationPlan(
    [user("user-schema")],
    [{ id: "user-schema", data: { email: "ok@example.com", phone: "", committee: "EVENTS", admin: true } }],
    [{ id: "user-schema", data: { fcmTokens: ["ok"], debug: true } }],
  );

  assert.ok(result.issues.some((issue) => issue.field === "userContacts.admin"));
  assert.ok(result.issues.some((issue) => issue.field === "userDevices.debug"));
});

test("reports invalid records and orphan documents without exposing contact values", () => {
  const invalid = user("bad-user", { committee: "UNKNOWN", email: "private@example.com" });
  const plan = buildContactMigrationPlan(
    [invalid],
    [{ id: "orphan-contact", data: { email: "secret@example.com" } }],
    [{ id: "orphan-device", data: { fcmTokens: ["private-token"] } }],
  );
  const report = buildContactMigrationReport(plan);

  assert.equal(report.totalProfiles, 1);
  assert.equal(report.validProfiles, 0);
  assert.equal(report.invalidProfiles, 1);
  assert.equal(report.skippedOrphans, 2);
  assert.deepEqual(plan.skipped, [
    { uid: "orphan-contact", collections: ["userContacts"] },
    { uid: "orphan-device", collections: ["userDevices"] },
  ]);
  assert.equal(JSON.stringify(report).includes("private@example.com"), false);
  assert.equal(JSON.stringify(report).includes("private-token"), false);
});

test("empty datasets report explicitly and never write", async () => {
  let writes = 0;
  const result = await runContactMigration([], [], [], { apply: true }, async () => { writes += 1; });

  assert.equal(result.status, "dry-run");
  assert.equal(result.report.emptyDataset, true);
  assert.equal(result.report.totalProfiles, 0);
  assert.equal(result.report.plannedBatches, 0);
  assert.equal(writes, 0);
});

test("preflight failure prevents every production-writer callback", async () => {
  let writes = 0;
  const bad = user("bad-user");
  delete bad.data.profilePhotoUrl;
  const result = await runContactMigration([bad], [], [], { apply: true }, async () => { writes += 1; });

  assert.equal(result.status, "preflight-failed");
  assert.equal(result.completedBatches, 0);
  assert.equal(writes, 0);
});

test("migration is idempotent across repeated runs", async () => {
  const storedUser = user("user-3", {
    email: "legacy@example.com",
    phone: "+1",
    fcmTokens: ["token", "token"],
  });
  let storedContact;
  let storedDevice;
  const writeBatch = async (records) => {
    for (const record of records) {
      storedContact = { ...record.contact };
      if (record.writeDeviceDocument) storedDevice = { fcmTokens: [...record.fcmTokens] };
      if (record.removeLegacyFields) {
        delete storedUser.data.email;
        delete storedUser.data.phone;
        delete storedUser.data.fcmTokens;
      }
    }
  };

  const first = await runContactMigration([storedUser], [], [], { apply: true }, writeBatch);
  const second = await runContactMigration(
    [storedUser],
    [{ id: storedUser.id, data: storedContact }],
    [{ id: storedUser.id, data: storedDevice }],
    { apply: true },
    writeBatch,
  );

  assert.equal(first.status, "applied");
  assert.equal(second.status, "applied");
  assert.deepEqual(second.plan.issues, []);
  assert.deepEqual(storedContact, { email: "legacy@example.com", phone: "+1", committee: "EVENTS" });
  assert.deepEqual(storedDevice, { fcmTokens: ["token"] });
  assert.equal(second.plan.records[0].removeLegacyFields, false);
});

test("batch failures stop later writes and report safe resume progress", async () => {
  const users = Array.from({ length: 301 }, (_, index) => user(`member-${index}`));
  let calls = 0;
  await assert.rejects(
    runContactMigration(users, [], [], { apply: true }, async () => {
      calls += 1;
      if (calls === 2) throw new Error("simulated commit failure");
    }),
    (error) => {
      assert.ok(error instanceof MigrationBatchError);
      assert.equal(error.failedBatchNumber, 2);
      assert.equal(error.totalBatches, 3);
      assert.equal(error.completedBatches, 1);
      assert.equal(error.migratedProfiles, 150);
      return true;
    },
  );
  assert.equal(calls, 2, "no batch after the failed batch may be attempted");
});

test("migration defaults to dry-run and requires an explicit apply flag", () => {
  assert.deepEqual(parseMigrationArguments(["--project-id", "ecell-test"], undefined), {
    projectId: "ecell-test",
    apply: false,
    help: false,
  });
  assert.equal(parseMigrationArguments(["--project-id", "ecell-test", "--apply"]).apply, true);
  assert.throws(() => parseMigrationArguments([], undefined), /Pass --project-id/);
});
