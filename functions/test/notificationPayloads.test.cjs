const test = require("node:test");
const assert = require("node:assert/strict");
const { complaintUpdateNotification } = require("../lib/notificationPayloads.js");

test("complaint update push payload uses fixed generic wording", () => {
  const payload = complaintUpdateNotification();

  assert.deepEqual(payload, {
    title: "Complaint update",
    body: "Your complaint status or response changed. Sign in to view details.",
  });
  assert.equal(Object.hasOwn(payload, "complaintTitle"), false);
  assert.equal(Object.hasOwn(payload, "complaintBody"), false);
});
