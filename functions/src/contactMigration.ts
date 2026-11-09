const validCommittees = new Set([
  "NONE",
  "EVENTS",
  "CORPORATE",
  "MARKETING",
  "DESIGN",
  "SOCIAL_MEDIA",
  "STARTUP",
  "WEB_TECH",
  "STRATEGY",
]);

const validRoles = new Set(["PENDING", "MEMBER", "COMMITTEE_HEAD", "COORDINATOR"]);
const publicProfileFields = new Set([
  "uid", "name", "year", "branch", "committee", "role", "bio",
  "profilePhotoUrl", "joinedAt", "isApproved",
]);
const privateProfileFields = ["email", "phone", "fcmTokens"] as const;
const contactFields = new Set(["email", "phone", "committee"]);
const deviceFields = new Set(["fcmTokens"]);
const maxDeviceTokensPerUser = 20;
const maxFcmTokenLength = 4096;

export interface MigrationDocument {
  id: string;
  data: Record<string, unknown>;
}

export interface ContactMigrationRecord {
  uid: string;
  contact: { email: string; phone: string; committee: string };
  fcmTokens: string[];
  writeDeviceDocument: boolean;
  removeLegacyFields: boolean;
}

export interface ContactMigrationIssue {
  uid: string;
  field: string;
  message: string;
}

export interface ContactMigrationSkippedDocument {
  uid: string;
  collections: Array<"userContacts" | "userDevices">;
}

export interface ContactMigrationPlan {
  records: ContactMigrationRecord[];
  issues: ContactMigrationIssue[];
  skipped: ContactMigrationSkippedDocument[];
}

export interface ContactMigrationReport {
  totalProfiles: number;
  validProfiles: number;
  invalidProfiles: number;
  skippedOrphans: number;
  contactsToWrite: number;
  devicesToWrite: number;
  profilesToClean: number;
  uniqueTokensToMove: number;
  plannedBatches: number;
  emptyDataset: boolean;
}

export interface ContactMigrationRunResult {
  status: "preflight-failed" | "dry-run" | "applied";
  plan: ContactMigrationPlan;
  report: ContactMigrationReport;
  completedBatches: number;
  migratedProfiles: number;
}

export interface MigrationArguments {
  projectId: string;
  apply: boolean;
  help: boolean;
}

function addIssue(
  issues: ContactMigrationIssue[],
  uid: string,
  field: string,
  message: string,
): void {
  issues.push({ uid, field, message });
}

function readContactValue(
  uid: string,
  field: "email" | "phone",
  current: Record<string, unknown>,
  legacy: Record<string, unknown>,
  maxLength: number,
  issues: ContactMigrationIssue[],
): string {
  const existing = current[field];
  const old = legacy[field];
  if (existing !== undefined && typeof existing !== "string") {
    addIssue(issues, uid, `userContacts.${field}`, "expected a string");
  }
  if (old !== undefined && typeof old !== "string") {
    addIssue(issues, uid, `users.${field}`, "expected a string");
  }

  const value = (typeof existing === "string" && existing.trim() ? existing :
    typeof old === "string" ? old : "").trim();
  if (value.length > maxLength) {
    addIssue(issues, uid, field, `exceeds ${maxLength} characters`);
  }
  if (field === "email" && value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    addIssue(issues, uid, field, "is not a valid email address");
  }
  return value;
}

function readTokens(
  uid: string,
  source: Record<string, unknown>,
  field: string,
  issues: ContactMigrationIssue[],
): string[] {
  const value = source.fcmTokens;
  if (value === undefined) return [];
  if (!Array.isArray(value)) {
    addIssue(issues, uid, field, "expected an array of strings");
    return [];
  }

  const tokens: string[] = [];
  value.forEach((token, index) => {
    if (typeof token !== "string" || token.length === 0 || token.length > maxFcmTokenLength) {
      addIssue(issues, uid, `${field}[${index}]`, "expected a non-empty string of at most 4096 characters");
      return;
    }
    tokens.push(token);
  });
  return tokens;
}

function validatePublicProfile(uid: string, data: Record<string, unknown>, issues: ContactMigrationIssue[]): void {
  const finalProfile = { ...data };
  for (const field of privateProfileFields) delete finalProfile[field];

  for (const field of publicProfileFields) {
    if (!Object.prototype.hasOwnProperty.call(finalProfile, field)) {
      addIssue(issues, uid, field, "required public profile field is missing");
    }
  }
  for (const field of Object.keys(finalProfile)) {
    if (!publicProfileFields.has(field)) {
      addIssue(issues, uid, field, "is not allowed by the public profile schema");
    }
  }

  if (finalProfile.uid !== undefined && (typeof finalProfile.uid !== "string" || finalProfile.uid !== uid)) {
    addIssue(issues, uid, "uid", "must be a string equal to the users document ID");
  }
  if (finalProfile.name !== undefined &&
      (typeof finalProfile.name !== "string" || finalProfile.name.length > 100)) {
    addIssue(issues, uid, "name", "must be a string of at most 100 characters");
  }
  if (finalProfile.year !== undefined &&
      (!Number.isInteger(finalProfile.year) || (finalProfile.year as number) < 1 || (finalProfile.year as number) > 8)) {
    addIssue(issues, uid, "year", "must be an integer between 1 and 8");
  }
  if (finalProfile.branch !== undefined &&
      (typeof finalProfile.branch !== "string" || finalProfile.branch.length > 100)) {
    addIssue(issues, uid, "branch", "must be a string of at most 100 characters");
  }
  if (finalProfile.committee !== undefined &&
      (typeof finalProfile.committee !== "string" || !validCommittees.has(finalProfile.committee))) {
    addIssue(issues, uid, "committee", "is not a supported committee");
  }
  if (finalProfile.role !== undefined &&
      (typeof finalProfile.role !== "string" || !validRoles.has(finalProfile.role))) {
    addIssue(issues, uid, "role", "is not a supported role");
  }
  if (finalProfile.bio !== undefined &&
      (typeof finalProfile.bio !== "string" || finalProfile.bio.length > 1000)) {
    addIssue(issues, uid, "bio", "must be a string of at most 1000 characters");
  }
  if (finalProfile.profilePhotoUrl !== undefined &&
      (typeof finalProfile.profilePhotoUrl !== "string" || finalProfile.profilePhotoUrl.length > 2048)) {
    addIssue(issues, uid, "profilePhotoUrl", "must be a string of at most 2048 characters");
  }
  if (finalProfile.joinedAt !== undefined &&
      (!Number.isInteger(finalProfile.joinedAt) || (finalProfile.joinedAt as number) < 0)) {
    addIssue(issues, uid, "joinedAt", "must be a non-negative integer timestamp");
  }
  if (finalProfile.isApproved !== undefined && typeof finalProfile.isApproved !== "boolean") {
    addIssue(issues, uid, "isApproved", "must be a boolean");
  }

  if (typeof finalProfile.isApproved === "boolean" &&
      typeof finalProfile.role === "string" && validRoles.has(finalProfile.role) &&
      typeof finalProfile.committee === "string" && validCommittees.has(finalProfile.committee)) {
    if (finalProfile.isApproved &&
        (!new Set(["MEMBER", "COMMITTEE_HEAD", "COORDINATOR"]).has(finalProfile.role) ||
         finalProfile.committee === "NONE")) {
      addIssue(issues, uid, "role/committee/isApproved", "approved profiles need an assigned committee and approved role");
    } else if (!finalProfile.isApproved &&
        (finalProfile.role !== "PENDING" || finalProfile.committee !== "NONE")) {
      addIssue(issues, uid, "role/committee/isApproved", "unapproved profiles must use PENDING and NONE");
    }
  }
}

function validateContactDocument(
  uid: string,
  current: Record<string, unknown>,
  profileCommittee: unknown,
  issues: ContactMigrationIssue[],
): void {
  for (const field of Object.keys(current)) {
    if (!contactFields.has(field)) {
      addIssue(issues, uid, `userContacts.${field}`, "is not allowed by the contact schema");
    }
  }
  if (current.committee !== undefined &&
      (typeof current.committee !== "string" || !validCommittees.has(current.committee))) {
    addIssue(issues, uid, "userContacts.committee", "is not a supported committee");
  } else if (current.committee !== undefined && current.committee !== profileCommittee) {
    addIssue(issues, uid, "userContacts.committee", "does not match the public profile committee");
  }
}

function validateDeviceDocument(uid: string, current: Record<string, unknown>, issues: ContactMigrationIssue[]): void {
  for (const field of Object.keys(current)) {
    if (!deviceFields.has(field)) {
      addIssue(issues, uid, `userDevices.${field}`, "is not allowed by the device schema");
    }
  }
}

export function buildContactMigrationPlan(
  users: MigrationDocument[],
  contacts: MigrationDocument[],
  devices: MigrationDocument[],
): ContactMigrationPlan {
  const contactsByUid = new Map(contacts.map((document) => [document.id, document.data]));
  const devicesByUid = new Map(devices.map((document) => [document.id, document.data]));
  const userIds = new Set(users.map((document) => document.id));
  const issues: ContactMigrationIssue[] = [];
  const records = users.map(({ id: uid, data }) => {
    const currentContact = contactsByUid.get(uid) ?? {};
    const currentDevice = devicesByUid.get(uid) ?? {};

    validatePublicProfile(uid, data, issues);
    validateContactDocument(uid, currentContact, data.committee, issues);
    validateDeviceDocument(uid, currentDevice, issues);

    const email = readContactValue(uid, "email", currentContact, data, 254, issues);
    const phone = readContactValue(uid, "phone", currentContact, data, 32, issues);
    const tokens = [
      ...readTokens(uid, currentDevice, "userDevices.fcmTokens", issues),
      ...readTokens(uid, data, "users.fcmTokens", issues),
    ];
    const uniqueTokens = [...new Set(tokens)];
    if (uniqueTokens.length > maxDeviceTokensPerUser) {
      addIssue(
        issues,
        uid,
        "userDevices.fcmTokens",
        `has ${uniqueTokens.length} unique tokens; the maximum is ${maxDeviceTokensPerUser}`,
      );
    }

    return {
      uid,
      contact: {
        email,
        phone,
        committee: typeof data.committee === "string" && validCommittees.has(data.committee)
          ? data.committee
          : "NONE",
      },
      fcmTokens: uniqueTokens,
      writeDeviceDocument: devicesByUid.has(uid) || uniqueTokens.length > 0,
      removeLegacyFields: privateProfileFields.some((field) => Object.prototype.hasOwnProperty.call(data, field)),
    };
  });

  const orphanCollections = new Map<string, Set<"userContacts" | "userDevices">>();
  for (const document of contacts) {
    if (!userIds.has(document.id)) {
      const collections = orphanCollections.get(document.id) ?? new Set();
      collections.add("userContacts");
      orphanCollections.set(document.id, collections);
    }
  }
  for (const document of devices) {
    if (!userIds.has(document.id)) {
      const collections = orphanCollections.get(document.id) ?? new Set();
      collections.add("userDevices");
      orphanCollections.set(document.id, collections);
    }
  }

  return {
    records,
    issues,
    skipped: [...orphanCollections.entries()].map(([uid, collections]) => ({
      uid,
      collections: [...collections],
    })),
  };
}

export function buildContactMigrationReport(plan: ContactMigrationPlan, batchSize = 150): ContactMigrationReport {
  if (!Number.isInteger(batchSize) || batchSize < 1 || batchSize > 150) {
    throw new Error("Batch size must be an integer between 1 and 150 profiles.");
  }
  const invalidUids = new Set(plan.issues.map((issue) => issue.uid));
  return {
    totalProfiles: plan.records.length,
    validProfiles: plan.records.length - invalidUids.size,
    invalidProfiles: invalidUids.size,
    skippedOrphans: plan.skipped.reduce((count, item) => count + item.collections.length, 0),
    contactsToWrite: plan.records.length,
    devicesToWrite: plan.records.filter((record) => record.writeDeviceDocument).length,
    profilesToClean: plan.records.filter((record) => record.removeLegacyFields).length,
    uniqueTokensToMove: plan.records.reduce((count, record) => count + record.fcmTokens.length, 0),
    plannedBatches: Math.ceil(plan.records.length / batchSize),
    emptyDataset: plan.records.length === 0,
  };
}

export class MigrationBatchError extends Error {
  constructor(
    readonly failedBatchNumber: number,
    readonly totalBatches: number,
    readonly completedBatches: number,
    readonly migratedProfiles: number,
    cause: unknown,
  ) {
    super(`Batch ${failedBatchNumber} of ${totalBatches} failed after ${completedBatches} completed batch(es).`, { cause });
    this.name = "MigrationBatchError";
  }
}

export async function runContactMigration(
  users: MigrationDocument[],
  contacts: MigrationDocument[],
  devices: MigrationDocument[],
  options: { apply: boolean; batchSize?: number },
  writeBatch: (records: ContactMigrationRecord[]) => Promise<void>,
): Promise<ContactMigrationRunResult> {
  const batchSize = options.batchSize ?? 150;
  const plan = buildContactMigrationPlan(users, contacts, devices);
  const report = buildContactMigrationReport(plan, batchSize);

  if (plan.issues.length > 0) {
    return { status: "preflight-failed", plan, report, completedBatches: 0, migratedProfiles: 0 };
  }
  if (!options.apply || report.emptyDataset) {
    return { status: "dry-run", plan, report, completedBatches: 0, migratedProfiles: 0 };
  }

  let completedBatches = 0;
  let migratedProfiles = 0;
  for (let offset = 0; offset < plan.records.length; offset += batchSize) {
    const batch = plan.records.slice(offset, offset + batchSize);
    const batchNumber = completedBatches + 1;
    try {
      await writeBatch(batch);
    } catch (error) {
      throw new MigrationBatchError(batchNumber, report.plannedBatches, completedBatches, migratedProfiles, error);
    }
    completedBatches += 1;
    migratedProfiles += batch.length;
  }

  return { status: "applied", plan, report, completedBatches, migratedProfiles };
}

export function parseMigrationArguments(
  args: string[],
  environmentProjectId?: string,
): MigrationArguments {
  let projectId = environmentProjectId?.trim() ?? "";
  let apply = false;
  let help = false;

  for (let index = 0; index < args.length; index += 1) {
    const argument = args[index];
    if (argument === "--project-id") {
      projectId = args[index + 1]?.trim() ?? "";
      if (!projectId) throw new Error("--project-id requires a non-empty value.");
      index += 1;
    } else if (argument === "--apply") {
      apply = true;
    } else if (argument === "--dry-run") {
      apply = false;
    } else if (argument === "--help" || argument === "-h") {
      help = true;
    } else if (!argument.startsWith("-") && !projectId) {
      // Keep the old positional project ID usable while the README migrates.
      projectId = argument.trim();
    } else {
      throw new Error(`Unknown migration argument: ${argument}`);
    }
  }

  if (!help && !projectId) {
    throw new Error("Pass --project-id <id> or set GCLOUD_PROJECT.");
  }
  return { projectId, apply, help };
}
