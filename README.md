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

Market news, meeting summaries, quizzes, and the local scoreboard described in older project notes are not part of the current app.

## Architecture

The Android client uses Compose screens, ViewModels, repository interfaces, Firestore-backed repository implementations, and Hilt dependency injection. `firestore.rules` is the authorization boundary; UI role checks are for presentation and are not a substitute for those rules.

Public member directory data is stored in `users/{uid}`. Private email and phone fields belong in `userContacts/{uid}` and are readable by the owner, coordinators, and the member's committee head. Push tokens belong in the owner-only `userDevices/{uid}` document. Announcements, events, and complaints are stored in their matching top-level collections.

Polls use `polls/{pollId}/voterReceipts/{uid}` to prevent repeat voting and `polls/{pollId}/ballots/{ballotId}` for choices. A receipt stores its owner's random ballot ID, while the ballot itself contains no voter UID; only that owner can read the receipt, so ordinary app users cannot link another member to a choice. Ballots stay unreadable while a poll is open and become visible after it closes. This is app-level privacy, not cryptographic anonymity: Firebase project administrators and Admin SDK code can read both collections and link voter UIDs to ballots. Direct messages use `conversations/{conversationId}/messages/{messageId}` and are readable only by the two approved participants. Messages are protected by Firestore access rules, but are not end-to-end encrypted.

## Firebase setup

1. Add the Android app to the Firebase project and place its `google-services.json` at `app/google-services.json`. The file is ignored by Git. Configure Google sign-in and add the SHA-1 for each signing variant; the Google services Gradle plugin supplies the web client ID used by the app.
2. Install the Firebase CLI and select the intended project with `firebase use --add`.
3. **Before deploying the tightened rules**, migrate existing public contact fields. Back up Firestore first, authenticate the Firebase Admin SDK through Application Default Credentials, install the Functions dependencies, and run:

   ```powershell
   gcloud auth application-default login
   cd functions
   npm install
   npm run migrate:contacts -- --project-id ecell-dcrust
   # Review the dry-run summary, then explicitly apply when ready:
   npm run migrate:contacts -- --project-id ecell-dcrust --apply
   cd ..
   ```

   Replace `ecell-dcrust` with the intended Firebase project ID. The first command is read-only by default. It validates every profile, contact and token before producing a summary; malformed records stop the run before any writes. Only the second command writes. It copies existing email and phone values to `userContacts`, moves FCM tokens to `userDevices`, then removes those private fields from `users`. Batches are bounded and reruns preserve existing contact values and de-duplicate tokens. Never run `--apply` until the project and the backup are confirmed.
4. Deploy the Firestore rules and notification functions:

   ```powershell
   firebase deploy --only firestore:rules,functions
   ```

   Voting and direct messages use Firestore directly and add no Cloud Functions dependency. Publish the updated `firestore.rules` before using them, then rebuild and install the Android app.

   This Firestore schema change is coordinated with the current Android app. Older app builds still write private fields to `users/{uid}` and cannot register tokens in `userDevices/{uid}`; plan a coordinated app/rules rollout and require users to update.

5. Sign in once with the first coordinator account. In Firestore, set that account's `users/{uid}` fields to `role: "COORDINATOR"` and `isApproved: true`. The account can then approve members in the app.

## Build

Use Android Studio or run `./gradlew assembleDebug` from the repository root. A local `app/google-services.json` is required by the Google Services Gradle plugin. The release build enables R8 and resource shrinking. Without the four signing variables, `assembleRelease` is unsigned. For a local signed APK, set `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`, then run `./gradlew assembleRelease`; keep the existing keystore and passwords out of Git. The Android version is `1.2.0` / version code `3` for the polls and messaging update.

The published-release workflow only creates a signed App Bundle when the protected `android-release` GitHub environment contains all four secrets: `ANDROID_KEYSTORE_BASE64` (an existing keystore encoded as base64), `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`. Configure required reviewers and limit allowed deployment tags/refs in that environment before adding secrets. The runner writes the decoded key under its temporary directory, verifies the resulting bundle, uploads it as a short-lived workflow artifact, and removes the temporary key. If the secrets are absent, no signed release artifact is produced and the workflow logs that the ordinary CI release build is unsigned. No signing key is generated by this repository.

The Firebase Functions are TypeScript in `functions/src`. Firebase deployment runs the TypeScript build through `firebase.json`.

## Security notes

- Coordinator and committee-head permissions are enforced in Firestore rules. Heads can create and manage content only for their own committee; organization-wide content is coordinator-only.
- Existing Firestore data must be migrated before deploying the rules. Directory reads for legacy documents are intentionally denied to other members while contact fields or FCM tokens remain on those records.
- CI builds the debug and release APKs, runs Android unit tests and lint, executes Firestore authorization regressions in the emulator, builds/tests the Functions migration helpers, and audits both Node dependency trees. The Android unit tests cover role-based navigation and list loading/error states; Firestore emulator tests exercise authentication, approval, committee privacy, voting, complaints, chat, and profile rules. Full tap-through UI testing still requires an Android emulator or device.
