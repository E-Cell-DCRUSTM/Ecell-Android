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

const privateProfileFields = ["email", "phone", "fcmTokens"] as const;

export interface MigrationDocument {
  id: string;
  data: Record<string, unknown>;
}

export interface ContactMigrationRecord {
  uid: string;
  contact: { email: string; phone: string; committee: string };
  fcmTokens: string[];
  removeLegacyFields: boolean;
}

export interface ContactMigrationIssue {
  uid: string;
  field: string;
  message: string;
}

export interface ContactMigrationPlan {
  records: ContactMigrationRecord[];
  issues: ContactMigrationIssue[];
}

export interface MigrationArguments {
  projectId: string;
  apply: boolean;
  help: boolean;
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
    issues.push({ uid, field: `userContacts.${field}`, message: "expected a string" });
  }
  if (old !== undefined && typeof old !== "string") {
    issues.push({ uid, field: `users.${field}`, message: "expected a string" });
  }

  const value = (typeof existing === "string" && existing.trim() ? existing :
    typeof old === "string" ? old : "").trim();
  if (value.length > maxLength) {
    issues.push({ uid, field, message: `exceeds ${maxLength} characters` });
  }
  if (field === "email" && value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    issues.push({ uid, field, message: "is not a valid email address" });
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
    issues.push({ uid, field, message: "expected an array of strings" });
    return [];
  }

  const tokens: string[] = [];
  for (const token of value) {
    if (typeof token !== "string" || token.length === 0 || token.length > 4096) {
      issues.push({ uid, field, message: "contains an invalid token" });
      continue;
    }
    tokens.push(token);
  }
  return tokens;
}

export function buildContactMigrationPlan(
  users: MigrationDocument[],
  contacts: MigrationDocument[],
  devices: MigrationDocument[],
): ContactMigrationPlan {
  const contactsByUid = new Map(contacts.map((document) => [document.id, document.data]));
  const devicesByUid = new Map(devices.map((document) => [document.id, document.data]));
  const issues: ContactMigrationIssue[] = [];
  const records = users.map(({ id: uid, data }) => {
    const currentContact = contactsByUid.get(uid) ?? {};
    const currentDevice = devicesByUid.get(uid) ?? {};
    const committee = data.committee;
    if (typeof committee !== "string" || !validCommittees.has(committee)) {
      issues.push({ uid, field: "committee", message: "is missing or not a supported committee" });
    }

    const email = readContactValue(uid, "email", currentContact, data, 254, issues);
    const phone = readContactValue(uid, "phone", currentContact, data, 32, issues);
    const tokens = [
      ...readTokens(uid, currentDevice, "userDevices.fcmTokens", issues),
      ...readTokens(uid, data, "users.fcmTokens", issues),
    ];

    return {
      uid,
      contact: {
        email,
        phone,
        committee: typeof committee === "string" && validCommittees.has(committee)
          ? committee
          : "NONE",
      },
      fcmTokens: [...new Set(tokens)],
      removeLegacyFields: privateProfileFields.some((field) => Object.hasOwn(data, field)),
    };
  });

  return { records, issues };
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
