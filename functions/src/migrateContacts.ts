import { initializeApp } from "firebase-admin/app";
import { FieldValue, getFirestore } from "firebase-admin/firestore";

const projectId = process.argv[2] ?? process.env.GCLOUD_PROJECT;
if (!projectId) {
  throw new Error("Pass the Firebase project id as an argument or set GCLOUD_PROJECT.");
}

initializeApp({ projectId });
const db = getFirestore();

async function migrateContacts(): Promise<void> {
  const users = await db.collection("users").get();
  const docs = users.docs;

  for (let offset = 0; offset < docs.length; offset += 150) {
    const batch = db.batch();
    for (const userDoc of docs.slice(offset, offset + 150)) {
      const data = userDoc.data();
      const contactRef = db.collection("userContacts").doc(userDoc.id);
      const deviceRef = db.collection("userDevices").doc(userDoc.id);
      const contact = await contactRef.get();
      const existing = contact.data() ?? {};
      const device = await deviceRef.get();
      const existingDevice = device.data() ?? {};
      const tokens = Array.from(new Set([
        ...(Array.isArray(existingDevice.fcmTokens) ? existingDevice.fcmTokens : []),
        ...(Array.isArray(data.fcmTokens) ? data.fcmTokens : []),
      ]));

      batch.set(contactRef, {
        email: existing.email || data.email || "",
        phone: existing.phone || data.phone || "",
        committee: data.committee ?? existing.committee ?? "NONE",
      }, { merge: true });

      batch.set(deviceRef, {
        fcmTokens: tokens,
      }, { merge: true });

      if (Object.hasOwn(data, "email") || Object.hasOwn(data, "phone") || Object.hasOwn(data, "fcmTokens")) {
        batch.update(userDoc.ref, {
          email: FieldValue.delete(),
          phone: FieldValue.delete(),
          fcmTokens: FieldValue.delete(),
        });
      }
    }
    await batch.commit();
  }

  console.log(`Migrated private contact fields for ${docs.length} user records in ${projectId}.`);
}

migrateContacts().catch((error: unknown) => {
  console.error("Contact migration failed.", error);
  process.exitCode = 1;
});
