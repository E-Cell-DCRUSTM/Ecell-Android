# E-Cell DCRUSTM Android

The Android app for E-Cell at DCRUSTM. It is built with Kotlin, Jetpack Compose, Firebase Authentication, Cloud Firestore, and Firebase Cloud Messaging.

## Implemented features

- Google sign-in with coordinator approval for new accounts.
- Member profiles, directory search, committee roster management, and coordinator approvals.
- Committee-scoped and organization-wide announcements.
- Upcoming events with date, time, location, and virtual meeting details.
- Private complaints submitted by members and reviewed by the coordinator.
- Push notifications for announcements, events, and complaint updates.
- Anonymous, one-person-one-vote polls with results hidden until the poll closes.
- Real-time one-to-one text conversations between approved members.
- Resumable five-step member onboarding, private enrollment/contact details, coordinator review, profile-photo upload, and an approved-member E-Cell ID with QR verification.

Market news, meeting summaries, quizzes, and the local scoreboard described in older project notes are not part of the current app.

## Architecture

The Android client uses Compose screens, ViewModels, repository interfaces, Firestore-backed repository implementations, and Hilt dependency injection. `firestore.rules` is the authorization boundary; UI role checks are for presentation and are not a substitute for those rules.

Public member directory data is stored in `users/{uid}`. Private email and phone fields belong in `userContacts/{uid}` and are readable by the owner, coordinators, and the member's committee head. Enrollment numbers, section and application feedback are kept in coordinator/owner-only `memberApplications/{uid}` documents. Push tokens belong in the owner-only `userDevices/{uid}` document. Profile photos use the owner-scoped `memberPhotos/{uid}/profile.jpg` Storage path. Member IDs are kept in the private application and a coordinator-readable `memberIdIndex`; QR values contain only the member ID. Announcements, events, and complaints are stored in their matching top-level collections.

Polls use `polls/{pollId}/voterReceipts/{uid}` to prevent repeat voting and `polls/{pollId}/ballots/{ballotId}` for choices. A receipt stores its owner's random ballot ID, while the ballot itself contains no voter UID; only that owner can read the receipt, so ordinary app users cannot link another member to a choice. Ballots stay unreadable while a poll is open and become visible after it closes. This is app-level privacy, not cryptographic anonymity: Firebase project administrators and Admin SDK code can read both collections and link voter UIDs to ballots. Direct messages use `conversations/{conversationId}/messages/{messageId}` and are readable only by the two approved participants. Messages are protected by Firestore access rules, but are not end-to-end encrypted.

Automatic Android app backup is disabled to reduce persistence of local authentication/session and Firestore cache data.

## Firebase setup

Start with [the staging and pilot guide](docs/STAGING_AND_PILOT.md). The current local `app/google-services.json` points to production `ecell-dcrust`; do not use it for testing. There is no verified staging project or alias in this checkout.

1. Add the Android app to a separate staging Firebase project and place that project's `google-services.json` at `app/google-services.json` locally. The file is ignored by Git. Configure the intended sign-in providers and add the debug SHA-1 for staging. The Google services Gradle plugin supplies the web client ID used by the app.
2. Install the Firebase CLI and select the staging project with `firebase use --add`. Verify the selected project before every Firebase command.
3. **Before deploying tightened rules to a project with existing data**, back up that project's Firestore data, authenticate the Firebase Admin SDK through Application Default Credentials, install the Functions dependencies, and run the migration in dry-run mode:

   ```powershell
   gcloud auth application-default login
   cd functions
   npm install
   npm run migrate:contacts -- --project-id <STAGING_PROJECT_ID>
   # Review the dry-run summary and staging backup before any explicit apply:
   npm run migrate:contacts -- --project-id <STAGING_PROJECT_ID> --apply
   cd ..
   ```

   The first command is read-only. It validates profiles, contact/device records and token limits before summarizing; malformed records stop before writes. Only `--apply` writes. It copies existing email and phone values to `userContacts`, moves FCM tokens to `userDevices`, then removes those private fields from `users`. Batches are bounded and reruns preserve existing contact values and de-duplicate tokens. Do not run `--apply` against production as part of staging setup.
4. Deploy reviewed Firestore and Storage rules to staging only:

   ```powershell
   firebase deploy --project <STAGING_PROJECT_ID> --only firestore:rules,storage
   ```

   Cloud Storage for Firebase requires the Blaze pay-as-you-go plan, including for the default bucket. No cloud bucket is created by this repository. If the staging project will not use Blaze, photo uploads are limited to emulator tests. Deploy Functions to staging separately only when notifications are ready to test. Voting and direct messages use Firestore directly and add no Cloud Functions dependency.

   The Firestore schema change is coordinated with the current Android app. Older app builds still write private fields to `users/{uid}` and cannot register tokens in `userDevices/{uid}`; plan a coordinated app/rules rollout and require users to update.

5. Create dedicated staging test accounts. After the staging coordinator signs in and their profile exists, use an authorized project administrator to promote only that staging account. It can then approve pilot test members through the app.

## Build

Use Android Studio with Android SDK 36 installed, or run `./gradlew assembleDebug` from the repository root. The app compiles and targets API 36. A local `app/google-services.json` is required by the Google Services Gradle plugin. The release build enables R8 and resource shrinking. Without the four signing variables, `assembleRelease` is unsigned. For a local signed APK, set `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`, then run `./gradlew assembleRelease`; keep the existing keystore and passwords out of Git. The Android version is `1.2.0` / version code `3` for the polls and messaging update.

The published-release workflow only creates a signed App Bundle when the protected `android-release` GitHub environment contains all four secrets: `ANDROID_KEYSTORE_BASE64` (an existing keystore encoded as base64), `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`. Configure required reviewers and limit allowed deployment tags/refs in that environment before adding secrets. The runner writes the decoded key under its temporary directory, verifies the resulting bundle, uploads it as a short-lived workflow artifact, and removes the temporary key. If the secrets are absent, no signed release artifact is produced and the workflow logs that the ordinary CI release build is unsigned. No signing key is generated by this repository.

The Firebase Functions are TypeScript in `functions/src`. Firebase deployment runs the TypeScript build through `firebase.json`. Approval notifications are sent only after a pending application becomes approved and require a deployed Function and a registered FCM token.

Phone number and enrollment number are optional by default. Local builds can require them with `-PecellProfilePhoneRequired=true` and/or `-PecellProfileEnrollmentRequired=true`; keep this policy consistent with the membership process. See the staging guide for manual onboarding, approval, photo, ID verification and pilot checks.

## Security notes

- Coordinator and committee-head permissions are enforced in Firestore rules. Heads can create and manage content only for their own committee; organization-wide content is coordinator-only.
- Existing Firestore data must be migrated before deploying the rules. Directory reads for legacy documents are intentionally denied to other members while contact fields or FCM tokens remain on those records.
- CI builds the debug and release APKs, runs Android unit tests and lint, executes Firestore authorization regressions in the emulator, builds/tests the Functions migration helpers, and audits both Node dependency trees. The Android unit tests cover role-based navigation and list loading/error states; Firestore emulator tests exercise authentication, approval, committee privacy, voting, complaints, chat, and profile rules. Full tap-through UI testing still requires an Android emulator or device.
