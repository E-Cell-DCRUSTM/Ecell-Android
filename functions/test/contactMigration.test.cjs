const assert = require("node:assert/strict");
const test = require("node:test");
const {
  buildContactMigrationPlan,
  parseMigrationArguments,
} = require("../lib/contactMigration.js");

test("prefers existing contact values and merges unique device tokens", () => {
  const result = buildContactMigrationPlan(
    [{ id: "user-1", data: { committee: "EVENTS", email: "legacy@example.com", phone: "111", fcmTokens: ["old", "shared"] } }],
    [{ id: "user-1", data: { email: "current@example.com", phone: "222", committee: "EVENTS" } }],
    [{ id: "user-1", data: { fcmTokens: ["shared", "new"] } }],
  );

  assert.deepEqual(result.issues, []);
  assert.deepEqual(result.records[0], {
    uid: "user-1",
    contact: { email: "current@example.com", phone: "222", committee: "EVENTS" },
    fcmTokens: ["shared", "new", "old"],
    removeLegacyFields: true,
  });
});

test("falls back to legacy contacts when existing contact values are blank", () => {
  const result = buildContactMigrationPlan(
    [{ id: "user-2", data: { committee: "DESIGN", email: "legacy@example.com", phone: "123" } }],
    [{ id: "user-2", data: { email: "", phone: "", committee: "DESIGN" } }],
    [],
  );

  assert.deepEqual(result.issues, []);
  assert.equal(result.records[0].contact.email, "legacy@example.com");
  assert.equal(result.records[0].contact.phone, "123");
});

test("reports invalid fields before any caller can apply a plan", () => {
  const result = buildContactMigrationPlan(
    [{ id: "bad-user", data: { committee: "UNKNOWN", email: 42, fcmTokens: "not-an-array" } }],
    [],
    [{ id: "bad-user", data: { fcmTokens: [null] } }],
  );

  assert.equal(result.records.length, 1);
  assert.deepEqual(result.issues.map((issue) => issue.field).sort(), [
    "committee",
    "userDevices.fcmTokens",
    "users.email",
    "users.fcmTokens",
  ]);
});

test("plan is idempotent after applying contact values and deleting legacy fields", () => {
  const users = [{ id: "user-3", data: { committee: "CORPORATE", email: "legacy@example.com", phone: "+1", fcmTokens: ["token"] } }];
  const first = buildContactMigrationPlan(users, [], []);
  const migratedUser = { ...users[0].data };
  delete migratedUser.email;
  delete migratedUser.phone;
  delete migratedUser.fcmTokens;
  const second = buildContactMigrationPlan(
    [{ id: "user-3", data: migratedUser }],
    [{ id: "user-3", data: first.records[0].contact }],
    [{ id: "user-3", data: { fcmTokens: first.records[0].fcmTokens } }],
  );

  assert.deepEqual(first.issues, []);
  assert.deepEqual(second.issues, []);
  assert.deepEqual(second.records[0].contact, first.records[0].contact);
  assert.deepEqual(second.records[0].fcmTokens, first.records[0].fcmTokens);
  assert.equal(second.records[0].removeLegacyFields, false);
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
