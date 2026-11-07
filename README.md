# E-Cell DCRUSTM Android

The Android app for E-Cell at DCRUSTM. It is built with Kotlin, Jetpack Compose, Firebase Authentication, Cloud Firestore, and Firebase Cloud Messaging.

## Implemented features

- Google sign-in with coordinator approval for new accounts.
- Member profiles, directory search, committee roster management, and coordinator approvals.
- Committee-scoped and organization-wide announcements.
- Upcoming events with date, time, location, and virtual meeting details.
- Private complaints submitted by members and reviewed by the coordinator.
- Push notifications for announcements, events, and complaint updates.

Market news, meeting summaries, quizzes, and the local scoreboard described in older project notes are not part of the current app.

## Architecture

The Android client uses Compose screens, ViewModels, repository interfaces, Firestore-backed repository implementations, and Hilt dependency injection. `firestore.rules` is the authorization boundary; UI role checks are for presentation and are not a substitute for those rules.

Public member directory data is stored in `users/{uid}`. Private email and phone fields belong in `userContacts/{uid}` and are readable by the owner, coordinators, and the member's committee head. Push tokens belong in the owner-only `userDevices/{uid}` document. Announcements, events, and complaints are stored in their matching top-level collections.

## Firebase setup

1. Add the Android app to the Firebase project and place its `google-services.json` at `app/google-services.json`. The file is ignored by Git. Configure Google sign-in and add the SHA-1 for each signing variant; the Google services Gradle plugin supplies the web client ID used by the app.
2. Install the Firebase CLI and select the intended project with `firebase use --add`.
3. **Before deploying the tightened rules**, migrate existing public contact fields. Back up Firestore first, authenticate the Firebase Admin SDK through Application Default Credentials, install the Functions dependencies, and run:

   ```powershell
   gcloud auth application-default login
   cd functions
   npm install
   npm run migrate:contacts -- ecell-dcrust
   cd ..
   ```

   Replace `ecell-dcrust` with the intended Firebase project ID. The migration copies existing email and phone values to `userContacts`, moves FCM tokens to `userDevices`, then removes those private fields from `users`. It is safe to rerun.
4. Deploy the Firestore rules and notification functions:

   ```powershell
   firebase deploy --only firestore:rules,functions
   ```

   This Firestore schema change is coordinated with the current Android app. Older app builds still write private fields to `users/{uid}` and cannot register tokens in `userDevices/{uid}`; plan a coordinated app/rules rollout and require users to update.

5. Sign in once with the first coordinator account. In Firestore, set that account's `users/{uid}` fields to `role: "COORDINATOR"` and `isApproved: true`. The account can then approve members in the app.

## Build

Use Android Studio or run `./gradlew assembleDebug` from the repository root. A local `app/google-services.json` is required by the Google Services Gradle plugin. The release build enables R8 and resource shrinking. For a Play-ready signed release, set `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD` in the build environment, then run `./gradlew assembleRelease`; keep the keystore and passwords out of Git. The Android version is `1.1.0` / version code `2` for this schema update.

The Firebase Functions are TypeScript in `functions/src`. Firebase deployment runs the TypeScript build through `firebase.json`.

## Security notes

- Coordinator and committee-head permissions are enforced in Firestore rules. Heads can create and manage content only for their own committee; organization-wide content is coordinator-only.
- Existing Firestore data must be migrated before deploying the rules. Directory reads for legacy documents are intentionally denied to other members while contact fields or FCM tokens remain on those records.
- The current test source files are Android Studio template tests and do not cover application flows yet.
