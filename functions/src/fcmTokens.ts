export const MAX_DEVICE_TOKENS_PER_USER = 20;
const MAX_FCM_TOKEN_LENGTH = 4096;

/** Defensively normalize legacy or malformed device-token documents before FCM calls. */
export function normalizeFcmTokens(value: unknown): string[] {
  if (!Array.isArray(value)) return [];
  return [...new Set(value.filter((token): token is string =>
    typeof token === "string" && token.length > 0 && token.length <= MAX_FCM_TOKEN_LENGTH))]
    .slice(0, MAX_DEVICE_TOKENS_PER_USER);
}
