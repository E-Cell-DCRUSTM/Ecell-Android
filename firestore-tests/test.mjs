import { readFileSync } from "node:fs";
import { createRequire } from "node:module";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { initializeTestEnvironment } from "@firebase/rules-unit-testing";
import {
  collection,
  deleteField,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
  where,
  writeBatch,
} from "firebase/firestore";
import { ref, uploadBytes, getBytes } from "firebase/storage";

const require = createRequire(import.meta.url);
const { runContactMigration } = require("../functions/lib/contactMigration.js");

const root = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const rules = readFileSync(resolve(root, "firestore.rules"), "utf8");
const storageRules = readFileSync(resolve(root, "storage.rules"), "utf8");
const env = await initializeTestEnvironment({
  projectId: "demo-ecell-dcrust",
  firestore: { rules },
  storage: { rules: storageRules },
});

const dbFor = (uid) => env.authenticatedContext(uid).firestore();
const storageFor = (uid) => env.authenticatedContext(uid).storage();
const alice = dbFor("alice");
const bob = dbFor("bob");
const carol = dbFor("carol");
const eventsHead = dbFor("events-head");
const corporateHead = dbFor("corporate-head");
const coordinator = dbFor("coordinator");
const pending = dbFor("pending");
let checks = 0;
let failures = 0;

async function check(label, allowed, action) {
  try {
    await action();
    checks += 1;
    if (allowed) console.log(`PASS ${label}`);
    else {
      failures += 1;
      console.error(`FAIL ${label}: unexpectedly allowed`);
    }
  } catch (error) {
    checks += 1;
    if (!allowed) console.log(`PASS ${label}: denied as expected`);
    else {
      failures += 1;
      console.error(`FAIL ${label}: ${error?.code ?? error}`);
    }
  }
}

function record(label, condition) {
  checks += 1;
  if (condition) console.log(`PASS ${label}`);
  else {
    failures += 1;
    console.error(`FAIL ${label}`);
  }
}

const profile = (uid, committee, role = "MEMBER", isApproved = true) => ({
  uid,
  name: uid,
  year: 2,
  branch: "CSE",
  committee,
  role,
  bio: "",
  profilePhotoUrl: "",
  joinedAt: 1,
  isApproved,
});

const memberApplication = (applicationStatus, overrides = {}) => ({
  enrollmentNumber: "",
  section: "",
  preferredCommittee: "NONE",
  profileCompleted: applicationStatus !== "INCOMPLETE" && applicationStatus !== "NEEDS_CORRECTION",
  applicationStatus,
  feedback: "",
  activeStep: applicationStatus === "INCOMPLETE" ? 1 : 5,
  memberId: "",
  createdAt: new Date(),
  updatedAt: new Date(),
  submittedAt: applicationStatus === "INCOMPLETE" ? null : new Date(),
  ...overrides,
});

const announcement = (authorUid, committee, overrides = {}) => ({
  title: "Scoped notice",
  body: "A useful update for E-Cell members.",
  authorUid,
  authorName: authorUid,
  committee,
  createdAt: 123,
  isPinned: false,
  attachmentUrl: "",
  ...overrides,
});

const eventData = (createdByUid, committee, overrides = {}) => ({
  title: "Workshop",
  description: "A practical workshop for members.",
  startTime: 456,
  location: "Seminar Hall",
  isVirtual: false,
  meetingLink: "",
  committee,
  createdByUid,
  createdByName: createdByUid,
  createdAt: 123,
  ...overrides,
});

const seed = async () => env.withSecurityRulesDisabled(async (context) => {
  const db = context.firestore();
  await Promise.all([
    setDoc(doc(db, "users/alice"), profile("alice", "EVENTS")),
    setDoc(doc(db, "users/bob"), profile("bob", "CORPORATE")),
    setDoc(doc(db, "users/carol"), profile("carol", "DESIGN")),
    setDoc(doc(db, "users/events-head"), profile("events-head", "EVENTS", "COMMITTEE_HEAD")),
    setDoc(doc(db, "users/corporate-head"), profile("corporate-head", "CORPORATE", "COMMITTEE_HEAD")),
    setDoc(doc(db, "users/head-applicant"), profile("head-applicant", "NONE", "PENDING", false)),
    setDoc(doc(db, "users/coordinator"), profile("coordinator", "NONE", "COORDINATOR")),
    setDoc(doc(db, "users/unapproved"), profile("unapproved", "NONE", "PENDING", false)),
    setDoc(doc(db, "memberApplications/head-applicant"), memberApplication("PENDING")),
    setDoc(doc(db, "memberApplications/unapproved"), memberApplication("PENDING")),
    setDoc(doc(db, "userContacts/alice"), { email: "alice@example.invalid", phone: "555-0100", committee: "EVENTS" }),
    setDoc(doc(db, "userContacts/head-applicant"), { email: "head@example.invalid", phone: "", committee: "NONE" }),
    setDoc(doc(db, "userContacts/unapproved"), { email: "unapproved@example.invalid", phone: "", committee: "NONE" }),
    setDoc(doc(db, "announcements/events"), { title: "Events", committee: "EVENTS" }),
    setDoc(doc(db, "announcements/corporate"), { title: "Corporate", committee: "CORPORATE" }),
    setDoc(doc(db, "announcements/org-wide"), { title: "All members", committee: null }),
    setDoc(doc(db, "announcements/missing-audience"), { title: "Malformed legacy record" }),
    setDoc(doc(db, "events/events"), { title: "Events", committee: "EVENTS" }),
    setDoc(doc(db, "events/corporate"), { title: "Corporate", committee: "CORPORATE" }),
    setDoc(doc(db, "events/org-wide"), { title: "All members", committee: null }),
    setDoc(doc(db, "events/missing-audience"), { title: "Malformed legacy record" }),
    setDoc(doc(db, "conversations/alice_bob"), {
      participants: ["alice", "bob"],
      createdByUid: "alice",
      createdAt: new Date(),
      updatedAt: new Date(),
      lastMessage: "",
      lastSenderUid: "",
      lastMessageId: "",
    }),
    setDoc(doc(db, "conversations/alice_bob/messages/seed"), {
      senderUid: "alice",
      text: "private message",
      sentAt: new Date(),
    }),
    setDoc(doc(db, "complaints/alice-complaint"), {
      title: "Private report",
      body: "A detailed description of the issue.",
      filerUid: "alice",
      filerName: "alice",
      filerCommittee: "EVENTS",
      status: "OPEN",
      response: "",
      createdAt: new Date(),
    }),
  ]);
});

async function createVote(db, pollId, uid, ballotId, optionIndex = 0) {
  const poll = doc(db, "polls", pollId);
  const batch = writeBatch(db);
  batch.set(doc(poll, "voterReceipts", uid), { voted: true, ballotId });
  batch.set(doc(poll, "ballots", ballotId), {
    optionIndex,
    castAt: serverTimestamp(),
  });
  await batch.commit();
}

try {
  await seed();

  const migrationUid = "migration-profile";
  const legacyUser = profile(migrationUid, "EVENTS");
  legacyUser.email = "migration@example.invalid";
  legacyUser.phone = "555-0140";
  legacyUser.fcmTokens = ["migration-token-a", "migration-token-b"];
  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), `users/${migrationUid}`), legacyUser);
  });
  const migrationInput = {
    users: [{ id: migrationUid, data: legacyUser }],
    contacts: [],
    devices: [],
  };
  const migrationResult = await runContactMigration(
    migrationInput.users,
    migrationInput.contacts,
    migrationInput.devices,
    { apply: true },
    async (records) => env.withSecurityRulesDisabled(async (context) => {
      const db = context.firestore();
      const batch = writeBatch(db);
      for (const migrated of records) {
        batch.set(doc(db, `userContacts/${migrated.uid}`), migrated.contact);
        if (migrated.writeDeviceDocument) {
          batch.set(doc(db, `userDevices/${migrated.uid}`), { fcmTokens: migrated.fcmTokens });
        }
        if (migrated.removeLegacyFields) {
          batch.update(doc(db, `users/${migrated.uid}`), {
            email: deleteField(),
            phone: deleteField(),
            fcmTokens: deleteField(),
          });
        }
      }
      await batch.commit();
    }),
  );
  record(
    "migration planner produces a valid profile/contact/device set",
    migrationResult.status === "applied" &&
      migrationResult.plan.issues.length === 0 &&
      migrationResult.report.validProfiles === 1,
  );
  const migratedUser = dbFor(migrationUid);
  await check("migrated public profile passes the current profile write rules", true, () =>
    updateDoc(doc(migratedUser, `users/${migrationUid}`), { bio: "Rules-verified migration" }));
  await check("migrated contact document passes the current contact rules", true, () =>
    getDoc(doc(migratedUser, `userContacts/${migrationUid}`)));
  await check("migrated device tokens pass the current device rules", true, () =>
    updateDoc(doc(migratedUser, `userDevices/${migrationUid}`), { fcmTokens: ["migration-token-c"] }));
  await check("another member cannot read the migrated private contact", false, () =>
    getDoc(doc(alice, `userContacts/${migrationUid}`)));

  const tinyJpeg = new Uint8Array([0xff, 0xd8, 0xff, 0xd9]);
  await check("member can upload a bounded JPEG to their own photo path", true, () =>
    uploadBytes(ref(storageFor("alice"), "memberPhotos/alice/profile.jpg"), tinyJpeg, { contentType: "image/jpeg" }));
  await check("approved members can read an approved member photo", true, () =>
    getBytes(ref(storageFor("bob"), "memberPhotos/alice/profile.jpg")));
  await check("member cannot overwrite another member's photo", false, () =>
    uploadBytes(ref(storageFor("alice"), "memberPhotos/bob/profile.jpg"), tinyJpeg, { contentType: "image/jpeg" }));
  await check("pending member can keep a photo private while awaiting review", true, () =>
    uploadBytes(ref(storageFor("unapproved"), "memberPhotos/unapproved/profile.jpg"), tinyJpeg, { contentType: "image/jpeg" }));
  await check("member cannot upload arbitrary files as profile photos", false, () =>
    uploadBytes(ref(storageFor("unapproved"), "memberPhotos/unapproved/profile.jpg"), tinyJpeg, { contentType: "application/pdf" }));
  await check("profile photo size is limited to 350 KB", false, () =>
    uploadBytes(ref(storageFor("unapproved"), "memberPhotos/unapproved/profile.jpg"), new Uint8Array(350 * 1024 + 1), { contentType: "image/jpeg" }));
  await check("pending member can read their own photo", true, () =>
    getBytes(ref(storageFor("unapproved"), "memberPhotos/unapproved/profile.jpg")));
  await check("approved member cannot read a pending member photo", false, () =>
    getBytes(ref(storageFor("alice"), "memberPhotos/unapproved/profile.jpg")));
  await check("coordinator can review a pending member photo", true, () =>
    getBytes(ref(storageFor("coordinator"), "memberPhotos/unapproved/profile.jpg")));
  await check("unapproved path patterns cannot expose unapproved users' photos", false, () =>
    getBytes(ref(storageFor("bob"), "memberPhotos/unapproved/profile.jpg")));

  await check("approved directory query", true, () =>
    getDocs(query(collection(alice, "users"), where("isApproved", "==", true))));
  await check("approved member cannot get a pending profile", false, () =>
    getDoc(doc(alice, "users/pending")));
  await check("approved member cannot query pending profiles", false, () =>
    getDocs(query(collection(alice, "users"), where("isApproved", "==", false))));
  await check("member can read their own private contact", true, () =>
    getDoc(doc(alice, "userContacts/alice")));
  await check("different committee member cannot read private contact", false, () =>
    getDoc(doc(bob, "userContacts/alice")));
  await check("same committee head can read private contact", true, () =>
    getDoc(doc(eventsHead, "userContacts/alice")));
  await check("different committee head cannot read private contact", false, () =>
    getDoc(doc(corporateHead, "userContacts/alice")));
  await check("member can update their own phone without losing committee", true, () =>
    updateDoc(doc(alice, "userContacts/alice"), { phone: "555-0101" }));
  await check("member cannot update their phone with too few digits", false, () =>
    updateDoc(doc(alice, "userContacts/alice"), { phone: "555-01" }));
  await check("member cannot update their phone with more than fifteen digits", false, () =>
    updateDoc(doc(alice, "userContacts/alice"), { phone: "1234567890123456" }));
  await check("member cannot update their phone with unsupported characters", false, () =>
    updateDoc(doc(alice, "userContacts/alice"), { phone: "555-01AB" }));
  await check("member cannot update their contact email with invalid syntax", false, () =>
    updateDoc(doc(alice, "userContacts/alice"), { email: "not-an-email" }));
  await check("member can create contact data with valid optional phone formatting", true, () =>
    setDoc(doc(carol, "userContacts/carol"), {
      email: "carol+test@example.edu",
      phone: " +91 (98765) 43210 ",
      committee: "DESIGN",
    }));
  await check("member can register bounded FCM tokens for their own devices", true, () =>
    setDoc(doc(alice, "userDevices/alice"), { fcmTokens: ["token-one", "token-two"] }));
  await check("member cannot read another member's FCM tokens", false, () =>
    getDoc(doc(alice, "userDevices/bob")));
  await check("member cannot write another member's FCM tokens", false, () =>
    setDoc(doc(alice, "userDevices/bob"), { fcmTokens: ["token-forged"] }));
  await check("device token values must be strings", false, () =>
    setDoc(doc(bob, "userDevices/bob"), { fcmTokens: [42] }));
  await check("device token values must be bounded strings", false, () =>
    setDoc(doc(bob, "userDevices/bob"), { fcmTokens: ["x".repeat(4097)] }));
  await check("device token lists have a bounded size", false, () =>
    setDoc(doc(bob, "userDevices/bob"), { fcmTokens: Array.from({ length: 21 }, (_, i) => `token-${i}`) }));

  await env.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await setDoc(doc(adminDb, "users/legacy"), {
      ...profile("legacy", "EVENTS"),
      email: "legacy@example.invalid",
      phone: "123",
      fcmTokens: ["private-token"],
    });
    await setDoc(doc(adminDb, "userContacts/legacy"), {
      email: "legacy@example.invalid",
      phone: "123",
      committee: "EVENTS",
    });
  });
  await check("other members cannot read legacy profile contact fields", false, () =>
    getDoc(doc(alice, "users/legacy")));
  await check("coordinator cannot read legacy contact fields before migration", false, () =>
    getDoc(doc(coordinator, "users/legacy")));
  await check("coordinator can read migrated contact details", true, () =>
    getDoc(doc(coordinator, "userContacts/legacy")));
  await check("new account can register with pending defaults", true, async () => {
    const batch = writeBatch(pending);
    batch.set(doc(pending, "users/pending"), profile("pending", "NONE", "PENDING", false));
    batch.set(doc(pending, "memberApplications/pending"), {
      ...memberApplication("INCOMPLETE", { createdAt: serverTimestamp(), updatedAt: serverTimestamp(), submittedAt: null }),
    });
    await batch.commit();
    await setDoc(doc(pending, "userContacts/pending"), {
      email: "pending@example.invalid",
      phone: "",
      committee: "NONE",
    });
  });
  await check("member can read their private enrollment application", true, () =>
    getDoc(doc(pending, "memberApplications/pending")));
  await check("another member cannot read private enrollment information", false, () =>
    getDoc(doc(alice, "memberApplications/pending")));
  await check("member cannot inject enrollment data into the public profile", false, () =>
    updateDoc(doc(pending, "users/pending"), { enrollmentNumber: "private-roll-42" }));
  await check("member can submit a complete profile and move it to coordinator review", true, async () => {
    const batch = writeBatch(pending);
    batch.update(doc(pending, "users/pending"), { year: 2, branch: "Computer Engineering" });
    batch.update(doc(pending, "userContacts/pending"), { phone: "555-0133" });
    batch.update(doc(pending, "memberApplications/pending"), {
      enrollmentNumber: "ROLL-42",
      section: "A",
      preferredCommittee: "DESIGN",
      activeStep: 5,
      updatedAt: serverTimestamp(),
    });
    await batch.commit();
    await updateDoc(doc(pending, "memberApplications/pending"), {
      profileCompleted: true,
      applicationStatus: "PENDING",
      feedback: "",
      activeStep: 5,
      submittedAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    });
  });
  await check("coordinator can review enrollment details", true, () =>
    getDoc(doc(coordinator, "memberApplications/pending")));
  await check("committee head cannot read enrollment details", false, () =>
    getDoc(doc(eventsHead, "memberApplications/pending")));
  await check("coordinator can read pending profiles", true, () =>
    getDoc(doc(coordinator, "users/pending")));
  await check("coordinator can approve and assign a committee atomically", true, async () => {
    const batch = writeBatch(coordinator);
    const memberId = "EC-AAAAAAAAAAAAAAAA";
    batch.update(doc(coordinator, "users/pending"), {
      isApproved: true,
      committee: "EVENTS",
      role: "MEMBER",
    });
    batch.set(doc(coordinator, "userContacts/pending"), { committee: "EVENTS" }, { merge: true });
    batch.update(doc(coordinator, "memberApplications/pending"), {
      applicationStatus: "APPROVED",
      profileCompleted: true,
      feedback: "",
      memberId,
      updatedAt: serverTimestamp(),
    });
    batch.set(doc(coordinator, `memberIdIndex/${memberId}`), { uid: "pending", createdAt: serverTimestamp() });
    batch.set(doc(collection(coordinator, "memberApplicationAudit/pending/events")), {
      uid: "pending", actorUid: "coordinator", action: "APPROVED", message: "", committee: "EVENTS",
      role: "MEMBER", memberId, at: serverTimestamp(),
    });
    await batch.commit();
  });
  await check("coordinator can approve an applicant as committee head", true, async () => {
    const batch = writeBatch(coordinator);
    const memberId = "EC-BBBBBBBBBBBBBBBB";
    batch.update(doc(coordinator, "users/head-applicant"), {
      isApproved: true,
      committee: "DESIGN",
      role: "COMMITTEE_HEAD",
    });
    batch.set(doc(coordinator, "userContacts/head-applicant"), { committee: "DESIGN" }, { merge: true });
    batch.update(doc(coordinator, "memberApplications/head-applicant"), {
      applicationStatus: "APPROVED",
      profileCompleted: true,
      feedback: "",
      memberId,
      updatedAt: serverTimestamp(),
    });
    batch.set(doc(coordinator, `memberIdIndex/${memberId}`), { uid: "head-applicant", createdAt: serverTimestamp() });
    batch.set(doc(collection(coordinator, "memberApplicationAudit/head-applicant/events")), {
      uid: "head-applicant", actorUid: "coordinator", action: "APPROVED", message: "", committee: "DESIGN",
      role: "COMMITTEE_HEAD", memberId, at: serverTimestamp(),
    });
    await batch.commit();
  });
  const legacyMemberId = "EC-CCCCCCCCCCCCCCCC";
  await check("approved legacy member can issue a unique ID only to their own account", true, async () => {
    const batch = writeBatch(alice);
    batch.set(doc(alice, "memberApplications/alice"), memberApplication("APPROVED", {
      profileCompleted: true,
      memberId: legacyMemberId,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
      submittedAt: null,
    }));
    batch.set(doc(alice, `memberIdIndex/${legacyMemberId}`), {
      uid: "alice", createdAt: serverTimestamp(),
    });
    await batch.commit();
  });
  await check("member can read their own private member ID", true, () =>
    getDoc(doc(alice, "memberApplications/alice")));
  await check("coordinator can verify a member ID", true, () =>
    getDoc(doc(coordinator, `memberIdIndex/${legacyMemberId}`)));
  await check("another approved member cannot inspect a different member ID index", false, () =>
    getDoc(doc(bob, `memberIdIndex/${legacyMemberId}`)));
  await check("member ID index cannot be enumerated", false, () =>
    getDocs(collection(alice, "memberIdIndex")));
  await check("coordinator cannot promote a member to coordinator in-app", false, () =>
    updateDoc(doc(coordinator, "users/alice"), {
      isApproved: true,
      committee: "EVENTS",
      role: "COORDINATOR",
    }));
  await check("coordinator can appoint a committee head", true, () =>
    updateDoc(doc(coordinator, "users/alice"), {
      isApproved: true,
      committee: "EVENTS",
      role: "COMMITTEE_HEAD",
    }));
  await check("coordinator cannot change their own role document", false, () =>
    updateDoc(doc(coordinator, "users/coordinator"), { role: "MEMBER" }));
  await check("member cannot promote themselves", false, () =>
    updateDoc(doc(alice, "users/alice"), { role: "COORDINATOR" }));
  await check("member can update their own profile fields", true, () =>
    updateDoc(doc(alice, "users/alice"), { bio: "A short member bio" }));
  await check("approved member cannot change academic year without coordinator review", false, () =>
    updateDoc(doc(alice, "users/alice"), { year: 4 }));
  await check("committee head cannot reassign another member", false, () =>
    updateDoc(doc(eventsHead, "users/bob"), { committee: "EVENTS", role: "MEMBER" }));

  await check("same-committee announcement get is allowed", true, () =>
    getDoc(doc(alice, "announcements/events")));
  await check("cross-committee announcement get is denied", false, () =>
    getDoc(doc(alice, "announcements/corporate")));
  await check("organization-wide announcement get is allowed", true, () =>
    getDoc(doc(alice, "announcements/org-wide")));
  await check("announcement with missing audience is denied to members", false, () =>
    getDoc(doc(alice, "announcements/missing-audience")));
  await check("committee announcement query matches the rules", true, () =>
    getDocs(query(collection(alice, "announcements"), where("committee", "==", "EVENTS"))));
  await check("cross-committee announcement query is denied", false, () =>
    getDocs(query(collection(alice, "announcements"), where("committee", "==", "CORPORATE"))));
  await check("org-wide announcement query matches the rules", true, () =>
    getDocs(query(collection(alice, "announcements"), where("committee", "==", null))));
  await check("same-committee event get is allowed", true, () =>
    getDoc(doc(alice, "events/events")));
  await check("cross-committee event get is denied", false, () =>
    getDoc(doc(alice, "events/corporate")));
  await check("organization-wide event get is allowed", true, () =>
    getDoc(doc(alice, "events/org-wide")));
  await check("event with missing audience is denied to members", false, () =>
    getDoc(doc(alice, "events/missing-audience")));
  await check("event committee query matches the rules", true, () =>
    getDocs(query(collection(alice, "events"), where("committee", "==", "EVENTS"))));
  await check("coordinator can read all committee events", true, () =>
    getDocs(collection(coordinator, "events")));

  await check("committee head can post to their committee", true, () =>
    setDoc(doc(eventsHead, "announcements/head-post"), announcement("events-head", "EVENTS")));
  await check("committee head cannot post organization-wide", false, () =>
    setDoc(doc(eventsHead, "announcements/head-wide"), announcement("events-head", null)));
  await check("committee head cannot post to another committee", false, () =>
    setDoc(doc(eventsHead, "announcements/head-cross-committee"), announcement("events-head", "CORPORATE")));
  await check("coordinator can post organization-wide", true, () =>
    setDoc(doc(coordinator, "announcements/coordinator-wide"), announcement("coordinator", null)));
  await check("member cannot create an announcement", false, () =>
    setDoc(doc(bob, "announcements/member-post"), announcement("bob", "CORPORATE")));
  await check("announcement creator UID must match the signed-in user", false, () =>
    setDoc(doc(eventsHead, "announcements/forged-uid"), announcement("alice", "EVENTS")));
  await check("announcement author name must match the profile", false, () =>
    setDoc(doc(eventsHead, "announcements/forged-name"), announcement("events-head", "EVENTS", { authorName: "Someone Else" })));
  await check("announcement requires every model field", false, () => {
    const malformed = announcement("events-head", "EVENTS");
    delete malformed.attachmentUrl;
    return setDoc(doc(eventsHead, "announcements/missing-field"), malformed);
  });
  await check("announcement rejects fields outside the model", false, () =>
    setDoc(doc(eventsHead, "announcements/extra-field"), announcement("events-head", "EVENTS", { hidden: true })));
  await check("announcement title length is enforced", false, () =>
    setDoc(doc(eventsHead, "announcements/long-title"), announcement("events-head", "EVENTS", { title: "x".repeat(121) })));
  await check("announcement body length is enforced", false, () =>
    setDoc(doc(eventsHead, "announcements/long-body"), announcement("events-head", "EVENTS", { body: "x".repeat(5001) })));
  await check("announcement author name length is enforced", false, () =>
    setDoc(doc(eventsHead, "announcements/long-author"), announcement("events-head", "EVENTS", { authorName: "x".repeat(101) })));
  await check("announcement attachment URL length is enforced", false, () =>
    setDoc(doc(eventsHead, "announcements/long-attachment"), announcement("events-head", "EVENTS", { attachmentUrl: "x".repeat(2049) })));
  await check("announcement pin state must be boolean", false, () =>
    setDoc(doc(eventsHead, "announcements/wrong-pin-type"), announcement("events-head", "EVENTS", { isPinned: "yes" })));
  await check("announcement cannot target the NONE committee", false, () =>
    setDoc(doc(coordinator, "announcements/invalid-audience"), announcement("coordinator", "NONE")));
  await check("committee head can update their own announcement", true, () =>
    updateDoc(doc(eventsHead, "announcements/head-post"), { title: "Updated events notice" }));
  await check("committee head cannot change their announcement audience", false, () =>
    updateDoc(doc(eventsHead, "announcements/head-post"), { committee: "MARKETING" }));
  await check("announcement author UID is immutable", false, () =>
    updateDoc(doc(eventsHead, "announcements/head-post"), { authorUid: "alice" }));
  await check("announcement author name is immutable", false, () =>
    updateDoc(doc(eventsHead, "announcements/head-post"), { authorName: "Changed name" }));
  await check("announcement creation time is immutable", false, () =>
    updateDoc(doc(eventsHead, "announcements/head-post"), { createdAt: 999 }));
  await check("different committee head cannot update an announcement", false, () =>
    updateDoc(doc(corporateHead, "announcements/head-post"), { title: "Cross-committee edit" }));
  await check("different committee head cannot delete an announcement", false, () =>
    deleteDoc(doc(corporateHead, "announcements/head-post")));
  await check("coordinator can update announcement content", true, () =>
    updateDoc(doc(coordinator, "announcements/coordinator-wide"), { title: "Coordinator edit" }));
  await check("committee head can delete their announcement", true, () =>
    deleteDoc(doc(eventsHead, "announcements/head-post")));
  await check("coordinator can delete managed announcement", true, () =>
    deleteDoc(doc(coordinator, "announcements/coordinator-wide")));

  await check("committee head can create an in-person event", true, () =>
    setDoc(doc(eventsHead, "events/head-in-person"), eventData("events-head", "EVENTS")));
  await check("coordinator can create an organization-wide virtual event", true, () =>
    setDoc(doc(coordinator, "events/coordinator-virtual"), eventData("coordinator", null, {
      isVirtual: true,
      location: "",
      meetingLink: "https://meet.example.invalid/room",
    })));
  await check("committee head cannot create an event for another committee", false, () =>
    setDoc(doc(eventsHead, "events/head-cross-committee"), eventData("events-head", "CORPORATE")));
  await check("member cannot create an event", false, () =>
    setDoc(doc(bob, "events/member-create"), eventData("bob", "CORPORATE")));
  await check("event creator UID must match the signed-in user", false, () =>
    setDoc(doc(eventsHead, "events/forged-uid"), eventData("alice", "EVENTS")));
  await check("event creator name must match the profile", false, () =>
    setDoc(doc(eventsHead, "events/forged-name"), eventData("events-head", "EVENTS", { createdByName: "Someone Else" })));
  await check("in-person event requires a location", false, () =>
    setDoc(doc(eventsHead, "events/missing-location"), eventData("events-head", "EVENTS", { location: "" })));
  await check("virtual event requires a meeting link", false, () =>
    setDoc(doc(eventsHead, "events/missing-link"), eventData("events-head", "EVENTS", {
      isVirtual: true,
      location: "",
      meetingLink: "",
    })));
  await check("event requires every model field", false, () => {
    const malformed = eventData("events-head", "EVENTS");
    delete malformed.createdAt;
    return setDoc(doc(eventsHead, "events/missing-field"), malformed);
  });
  await check("event rejects fields outside the model", false, () =>
    setDoc(doc(eventsHead, "events/extra-field"), eventData("events-head", "EVENTS", { hidden: true })));
  await check("event title length is enforced", false, () =>
    setDoc(doc(eventsHead, "events/long-title"), eventData("events-head", "EVENTS", { title: "x".repeat(121) })));
  await check("event description length is enforced", false, () =>
    setDoc(doc(eventsHead, "events/long-description"), eventData("events-head", "EVENTS", { description: "x".repeat(2001) })));
  await check("event location length is enforced", false, () =>
    setDoc(doc(eventsHead, "events/long-location"), eventData("events-head", "EVENTS", { location: "x".repeat(201) })));
  await check("event meeting URL length is enforced", false, () =>
    setDoc(doc(coordinator, "events/long-link"), eventData("coordinator", null, {
      isVirtual: true,
      location: "",
      meetingLink: "x".repeat(2049),
    })));
  await check("event creation time must be a nonnegative integer", false, () =>
    setDoc(doc(eventsHead, "events/wrong-time-type"), eventData("events-head", "EVENTS", { createdAt: "now" })));
  await check("committee head can update their own event", true, () =>
    updateDoc(doc(eventsHead, "events/head-in-person"), { title: "Updated workshop" }));
  await check("committee head cannot change their event audience", false, () =>
    updateDoc(doc(eventsHead, "events/head-in-person"), { committee: "MARKETING" }));
  await check("event creator UID is immutable", false, () =>
    updateDoc(doc(eventsHead, "events/head-in-person"), { createdByUid: "alice" }));
  await check("event creator name is immutable", false, () =>
    updateDoc(doc(eventsHead, "events/head-in-person"), { createdByName: "Changed name" }));
  await check("event creation time is immutable", false, () =>
    updateDoc(doc(eventsHead, "events/head-in-person"), { createdAt: 999 }));
  await check("different committee head cannot update an event", false, () =>
    updateDoc(doc(corporateHead, "events/head-in-person"), { title: "Cross-committee edit" }));
  await check("different committee head cannot delete an event", false, () =>
    deleteDoc(doc(corporateHead, "events/head-in-person")));
  await check("coordinator can update event content", true, () =>
    updateDoc(doc(coordinator, "events/coordinator-virtual"), { title: "Coordinator event edit" }));
  await check("committee head can delete their event", true, () =>
    deleteDoc(doc(eventsHead, "events/head-in-person")));
  await check("coordinator can delete managed event", true, () =>
    deleteDoc(doc(coordinator, "events/coordinator-virtual")));

  const validComplaint = {
    title: "Room equipment issue",
    body: "The projector has not worked during the last three meetings.",
    filerUid: "alice",
    filerName: "alice",
    filerCommittee: "EVENTS",
    status: "OPEN",
    response: "",
    createdAt: serverTimestamp(),
  };
  await check("complaint with server timestamp and safe initial state is allowed", true, () =>
    setDoc(doc(alice, "complaints/new"), validComplaint));
  await check("member cannot submit a pre-resolved complaint or forged response", false, () =>
    setDoc(doc(alice, "complaints/forged"), {
      ...validComplaint,
      status: "RESOLVED",
      response: "already handled",
    }));
  await check("member cannot forge complaint identity", false, () =>
    setDoc(doc(alice, "complaints/forged-owner"), { ...validComplaint, filerUid: "bob" }));
  await check("complaint body length is enforced", false, () =>
    setDoc(doc(alice, "complaints/too-short"), { ...validComplaint, body: "short" }));
  await check("complaint filer can read their complaint", true, () =>
    getDoc(doc(alice, "complaints/alice-complaint")));
  await check("another member cannot read complaint details", false, () =>
    getDoc(doc(bob, "complaints/alice-complaint")));
  await check("filer-scoped complaint history query is allowed", true, () =>
    getDocs(query(collection(alice, "complaints"), where("filerUid", "==", "alice"))));
  await check("member cannot query another filer's complaint history", false, () =>
    getDocs(query(collection(bob, "complaints"), where("filerUid", "==", "alice"))));
  await check("coordinator can read complaints", true, () =>
    getDocs(collection(coordinator, "complaints")));
  await check("member cannot change complaint status or response", false, () =>
    updateDoc(doc(alice, "complaints/alice-complaint"), { status: "RESOLVED", response: "fake" }));
  await check("coordinator can resolve without changing filer data", true, () =>
    updateDoc(doc(coordinator, "complaints/alice-complaint"), {
      status: "RESOLVED",
      response: "The issue has been repaired.",
    }));
  await check("filer can read the coordinator response", true, async () => {
    const snapshot = await getDoc(doc(alice, "complaints/alice-complaint"));
    record("complaint response text is present for its filer", snapshot.data()?.response === "The issue has been repaired.");
  });
  await check("coordinator cannot rewrite complaint identity", false, () =>
    updateDoc(doc(coordinator, "complaints/alice-complaint"), { filerUid: "bob" }));

  const eventPoll = {
    title: "Next workshop",
    question: "Which day works best?",
    options: ["Monday", "Wednesday", "Friday"],
    audienceCommittee: "EVENTS",
    createdByUid: "events-head",
    createdByName: "events-head",
    isOpen: true,
    createdAt: serverTimestamp(),
  };
  await check("committee head can create a valid committee poll", true, () =>
    setDoc(doc(eventsHead, "polls/events-poll"), eventPoll));
  await check("committee head cannot create poll for another committee", false, () =>
    setDoc(doc(eventsHead, "polls/wrong-audience"), { ...eventPoll, audienceCommittee: "CORPORATE" }));
  await check("poll option values must be bounded strings", false, () =>
    setDoc(doc(eventsHead, "polls/invalid-options"), { ...eventPoll, options: ["Yes", 7] }));
  await check("another committee cannot get this poll", false, () =>
    getDoc(doc(bob, "polls/events-poll")));
  await check("unfiltered poll query is denied when it includes another committee", false, () =>
    getDocs(collection(bob, "polls")));

  await check("voter can atomically create one receipt and one anonymous ballot", true, () =>
    createVote(alice, "events-poll", "alice", "ballot-1", 1));
  await check("voter can read only their own receipt", true, () =>
    getDoc(doc(alice, "polls/events-poll/voterReceipts/alice")));
  await check("another member cannot read the receipt", false, () =>
    getDoc(doc(bob, "polls/events-poll/voterReceipts/alice")));
  await check("receipt collection cannot be listed", false, () =>
    getDocs(collection(alice, "polls/events-poll/voterReceipts")));
  await check("open-poll ballot is hidden from voter", false, () =>
    getDoc(doc(alice, "polls/events-poll/ballots/ballot-1")));
  await check("open-poll ballot is hidden from coordinator", false, () =>
    getDoc(doc(coordinator, "polls/events-poll/ballots/ballot-1")));
  await check("repeat vote is denied", false, () =>
    createVote(alice, "events-poll", "alice", "ballot-repeat", 0));
  await check("receipt-only write is denied", false, () =>
    setDoc(doc(bob, "polls/events-poll/voterReceipts/bob"), { voted: true, ballotId: "orphan" }));
  await check("ballot-only write is denied", false, () =>
    setDoc(doc(bob, "polls/events-poll/ballots/orphan"), {
      optionIndex: 0,
      castAt: serverTimestamp(),
    }));
  await check("out-of-range choice is denied", false, () =>
    createVote(bob, "events-poll", "bob", "invalid-index", 3));
  await check("two ballots cannot be paired to one receipt in a batch", false, async () => {
    const poll = doc(bob, "polls/events-poll");
    const batch = writeBatch(bob);
    batch.set(doc(poll, "voterReceipts", "bob"), { voted: true, ballotId: "multi-a" });
    batch.set(doc(poll, "ballots", "multi-a"), { optionIndex: 0, castAt: serverTimestamp() });
    batch.set(doc(poll, "ballots", "multi-b"), { optionIndex: 1, castAt: serverTimestamp() });
    await batch.commit();
  });
  await check("coordinator cannot close a poll while changing other fields", false, () =>
    updateDoc(doc(coordinator, "polls/events-poll"), {
      isOpen: false,
      closedAt: serverTimestamp(),
      title: "Tampered title",
    }));
  await check("poll owner can close it one-way", true, () =>
    updateDoc(doc(eventsHead, "polls/events-poll"), { isOpen: false, closedAt: serverTimestamp() }));
  await check("closed poll exposes anonymous ballots to its eligible members", true, () =>
    getDocs(collection(alice, "polls/events-poll/ballots")));
  await check("closed poll ballot document still has no voter UID", true, async () => {
    const ballot = await getDoc(doc(alice, "polls/events-poll/ballots/ballot-1"));
    if (ballot.data()?.voterUid !== undefined) throw new Error("ballot contains a voter UID");
  });

  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "polls/race-poll"), { ...eventPoll, isOpen: true });
  });
  const raceResults = await Promise.allSettled([
    createVote(alice, "race-poll", "alice", "race-a", 0),
    createVote(alice, "race-poll", "alice", "race-b", 1),
  ]);
  record("concurrent duplicate submissions yield exactly one ballot", raceResults.filter((r) => r.status === "fulfilled").length === 1);
  await check("race leaves exactly one receipt", true, async () => {
    const receipt = await getDoc(doc(alice, "polls/race-poll/voterReceipts/alice"));
    if (!receipt.exists()) throw new Error("winner receipt is missing");
  });
  await check("race leaves exactly one ballot", true, () => env.withSecurityRulesDisabled(async (context) => {
    const ballots = await getDocs(collection(context.firestore(), "polls/race-poll/ballots"));
    if (ballots.size !== 1) throw new Error(`found ${ballots.size} ballots`);
  }));

  await check("participant can read an existing direct conversation", true, () =>
    getDoc(doc(alice, "conversations/alice_bob")));
  await check("nonparticipant cannot read an existing conversation", false, () =>
    getDoc(doc(carol, "conversations/alice_bob")));
  await check("nonparticipant cannot probe a missing conversation ID", false, () =>
    getDoc(doc(carol, "conversations/alice_carol")));
  await check("participant-filtered conversation list is allowed", true, () =>
    getDocs(query(collection(alice, "conversations"), where("participants", "array-contains", "alice"))));
  await check("unfiltered conversation collection query is denied", false, () =>
    getDocs(collection(carol, "conversations")));
  await check("canonical conversation creation is allowed", true, () =>
    setDoc(doc(alice, "conversations/alice_carol"), {
      participants: ["alice", "carol"],
      createdByUid: "alice",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
      lastMessage: "",
      lastSenderUid: "",
      lastMessageId: "",
    }));
  await check("conversation ID cannot be squatted for different participants", false, () =>
    setDoc(doc(alice, "conversations/bob_carol"), {
      participants: ["alice", "bob"],
      createdByUid: "alice",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
      lastMessage: "",
      lastSenderUid: "",
      lastMessageId: "",
    }));
  await check("conversation cannot include an unapproved account", false, () =>
    setDoc(doc(dbFor("unapproved"), "conversations/carol_unapproved"), {
      participants: ["carol", "unapproved"],
      createdByUid: "unapproved",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
      lastMessage: "",
      lastSenderUid: "",
      lastMessageId: "",
    }));
  await check("participants can atomically send a text and update preview", true, async () => {
    const thread = doc(alice, "conversations/alice_bob");
    const message = doc(thread, "messages", "new-message");
    const batch = writeBatch(alice);
    batch.set(message, { senderUid: "alice", text: "Hello", sentAt: serverTimestamp() });
    batch.update(thread, {
      lastMessage: "Hello",
      lastSenderUid: "alice",
      lastMessageId: "new-message",
      updatedAt: serverTimestamp(),
    });
    await batch.commit();
  });
  await check("participant can read thread messages", true, () =>
    getDocs(collection(bob, "conversations/alice_bob/messages")));
  await check("outsider cannot read thread messages", false, () =>
    getDocs(collection(carol, "conversations/alice_bob/messages")));
  await check("message sender cannot impersonate another participant", false, () =>
    setDoc(doc(alice, "conversations/alice_bob/messages/spoof"), {
      senderUid: "bob",
      text: "spoofed",
      sentAt: serverTimestamp(),
    }));

  await check("coordinator can reassign a member and update their contact committee atomically", true, async () => {
    const batch = writeBatch(coordinator);
    batch.update(doc(coordinator, "users/alice"), {
      committee: "CORPORATE",
      role: "MEMBER",
    });
    batch.set(doc(coordinator, "userContacts/alice"), { committee: "CORPORATE" }, { merge: true });
    await batch.commit();
  });

  await check("coordinator cannot delete an approved member profile", false, () =>
    deleteDoc(doc(coordinator, "users/bob")));
  await check("coordinator cannot delete their own profile", false, () =>
    deleteDoc(doc(coordinator, "users/coordinator")));
  await check("coordinator cannot erase a pending profile during rejection", false, () =>
    deleteDoc(doc(coordinator, "users/unapproved")));
  await check("coordinator can reject while retaining the application and feedback", true, async () => {
    await updateDoc(doc(coordinator, "memberApplications/unapproved"), {
      applicationStatus: "REJECTED",
      profileCompleted: true,
      feedback: "Please correct your department.",
      updatedAt: serverTimestamp(),
    });
    const rejected = await getDoc(doc(dbFor("unapproved"), "memberApplications/unapproved"));
    if (rejected.data()?.feedback !== "Please correct your department.") throw new Error("rejection feedback missing");
  });
  await check("rejected member can reapply without recreating the account", true, () =>
    updateDoc(doc(dbFor("unapproved"), "memberApplications/unapproved"), {
      applicationStatus: "INCOMPLETE",
      profileCompleted: false,
      feedback: "",
      activeStep: 1,
      submittedAt: null,
      updatedAt: serverTimestamp(),
    }));
} finally {
  await env.cleanup();
  console.log(`FIRESTORE RULES: ${checks - failures}/${checks} passed; ${failures} failed`);
}

if (failures > 0) process.exitCode = 1;
