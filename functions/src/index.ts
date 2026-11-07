import { onDocumentCreated, onDocumentUpdated } from "firebase-functions/v2/firestore";
import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";

initializeApp();
const db = getFirestore();
const messaging = getMessaging();

/**
 * Collects FCM tokens for every approved member, optionally scoped to one committee.
 * Tokens live in an owner-only userDevices/{uid} doc —
 * written client-side by MemberRepository.registerFcmToken.
 */
async function tokensForAudience(committee: string | null): Promise<string[]> {
  let query = db.collection("users").where("isApproved", "==", true) as FirebaseFirestore.Query;
  if (committee) {
    query = query.where("committee", "==", committee);
  }
  const snap = await query.get();
  if (snap.empty) return [];
  const deviceDocs = await db.getAll(
    ...snap.docs.map((doc) => db.collection("userDevices").doc(doc.id)),
  );
  const tokens: string[] = [];
  deviceDocs.forEach((doc) => {
    const data = doc.data();
    if (Array.isArray(data?.fcmTokens)) {
      tokens.push(...data.fcmTokens);
    }
  });
  return tokens;
}

async function tokensForUser(uid: string): Promise<string[]> {
  const doc = await db.collection("userDevices").doc(uid).get();
  if (!doc.exists) return [];
  const data = doc.data();
  return Array.isArray(data?.fcmTokens) ? data!.fcmTokens : [];
}

/** FCM caps a single multicast send at 500 tokens, so this chunks and fires sequentially. */
async function sendToTokens(tokens: string[], title: string, body: string): Promise<void> {
  const unique = Array.from(new Set(tokens)).filter((t) => typeof t === "string" && t.length > 0);
  if (unique.length === 0) return;

  for (let i = 0; i < unique.length; i += 500) {
    const chunk = unique.slice(i, i + 500);
    const response = await messaging.sendEachForMulticast({
      tokens: chunk,
      notification: { title, body },
    });

    // Clean up tokens Firebase reports as no longer valid (uninstalled app, etc.)
    const deadTokens: string[] = [];
    response.responses.forEach((r, idx) => {
      if (!r.success) {
        const code = r.error?.code;
        if (code === "messaging/registration-token-not-registered" || code === "messaging/invalid-argument") {
          deadTokens.push(chunk[idx]);
        }
      }
    });
    if (deadTokens.length > 0) {
      await pruneDeadTokens(deadTokens);
    }
  }
}

async function pruneDeadTokens(deadTokens: string[]): Promise<void> {
  // Tokens aren't indexed, so this is a best-effort sweep — fine at club scale (tens of users).
  const snap = await db.collection("userDevices").where("fcmTokens", "array-contains-any", deadTokens.slice(0, 10)).get();
  const batch = db.batch();
  snap.forEach((doc) => {
    const data = doc.data();
    const remaining = (data.fcmTokens as string[]).filter((t) => !deadTokens.includes(t));
    batch.update(doc.ref, { fcmTokens: remaining });
  });
  if (!snap.empty) await batch.commit();
}

export const onAnnouncementCreated = onDocumentCreated("announcements/{id}", async (event) => {
  const data = event.data?.data();
  if (!data) return;

  const committee = (data.committee as string | null) ?? null;
  const tokens = await tokensForAudience(committee);
  const audience = committee ? `${committee} committee` : "E-Cell";
  await sendToTokens(tokens, `New announcement · ${audience}`, data.title ?? "");
});

export const onEventCreated = onDocumentCreated("events/{id}", async (event) => {
  const data = event.data?.data();
  if (!data) return;

  const committee = (data.committee as string | null) ?? null;
  const tokens = await tokensForAudience(committee);
  await sendToTokens(tokens, "New event scheduled", data.title ?? "");
});

export const onComplaintUpdated = onDocumentUpdated("complaints/{id}", async (event) => {
  const before = event.data?.before.data();
  const after = event.data?.after.data();
  if (!before || !after) return;

  const statusChanged = before.status !== after.status;
  const responseChanged =
    before.response !== after.response && typeof after.response === "string" && after.response.length > 0;
  if (!statusChanged && !responseChanged) return;

  const filerUid = after.filerUid as string | undefined;
  if (!filerUid) return;

  const tokens = await tokensForUser(filerUid);
  await sendToTokens(tokens, "Your complaint was updated", after.title ?? "Status updated");
});
