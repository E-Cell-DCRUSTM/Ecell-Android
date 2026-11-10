const assert = require("node:assert/strict");
const test = require("node:test");
const { MAX_DEVICE_TOKENS_PER_USER, normalizeFcmTokens } = require("../lib/fcmTokens.js");

test("normalizes invalid and duplicate FCM token entries", () => {
  assert.deepEqual(normalizeFcmTokens([
    "first-token",
    "first-token",
    42,
    "",
    "x".repeat(4097),
    "second-token",
  ]), ["first-token", "second-token"]);
});

test("caps legacy FCM token documents at the supported per-user limit", () => {
  const tokens = Array.from({ length: MAX_DEVICE_TOKENS_PER_USER + 3 }, (_, index) => `token-${index}`);
  assert.equal(normalizeFcmTokens(tokens).length, MAX_DEVICE_TOKENS_PER_USER);
});

test("non-array legacy token data is ignored", () => {
  assert.deepEqual(normalizeFcmTokens({ token: "invalid-shape" }), []);
});
