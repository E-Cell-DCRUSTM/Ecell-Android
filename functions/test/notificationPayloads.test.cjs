const test = require("node:test");
const assert = require("node:assert/strict");
const {
  complaintUpdateNotification,
  membershipApprovalNotification,
} = require("../lib/notificationPayloads.js");

test("complaint update push payload uses fixed generic wording", () => {
  const payload = complaintUpdateNotification();

  assert.deepEqual(payload, {
    title: "Complaint update",
    body: "Your complaint status or response changed. Sign in to view details.",
  });
  assert.equal(Object.hasOwn(payload, "complaintTitle"), false);
  assert.equal(Object.hasOwn(payload, "complaintBody"), false);
});

test("membership approval notification contains no private profile details", () => {
  const payload = membershipApprovalNotification();
  assert.equal(payload.title, "E-Cell membership approved");
  assert.equal(payload.body, "Your membership is active. Sign in to view your E-Cell Member ID.");
  assert.doesNotMatch(`${payload.title} ${payload.body}`, /phone|enrollment|committee|email/i);
});
