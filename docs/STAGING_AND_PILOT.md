# Staging and E-Cell pilot guide

This guide is for a separate Firebase staging project and a controlled pilot with 5–10 trusted members. Do not use production accounts or data for these checks.

## Current environment status

Checked on 10 November 2026:

- The repository has no `.firebaserc` project alias and no verified staging Firebase project.
- The local, ignored `app/google-services.json` points to Firebase project `ecell-dcrust`, which is the production project. It must not be used for pilot testing.
- Firestore and Storage rules have been exercised locally against the `demo-ecell-dcrust` emulators. This does not prove a cloud staging deployment or a full Android journey.
- The configured Pixel_6_Pro AVD uses Android API 37.0. Android unit tests pass 23/23. The 11 instrumentation test cases passed across the full run and a focused retry of the one test whose selector was corrected. These screen tests use local UI state; they do not prove a Firebase login or full account journey.
- The Firestore and Storage emulator suite passes 177/177 checks; Functions tests pass 21/21. Debug and unsigned release builds succeed. Android Lint succeeds with 138 warnings and no errors.
- Cloud Storage for Firebase currently requires the Blaze pay-as-you-go plan, even for a default bucket. No project was upgraded and no bucket was created. Until a staging project has an authorized bucket, photo upload is verified only against the local Storage emulator. See [Firebase's Storage billing FAQ](https://firebase.google.com/docs/storage/faqs-storage-changes-announced-sept-2024).

## Create an isolated staging project

1. In Firebase Console, create a new project with a distinct ID such as `ecell-dcrust-staging`. Confirm the selected project ID in the console before configuring anything.
2. Register an Android app with package name `dcrustm.ecell.mobile`. Download that project's `google-services.json` and place it at `app/google-services.json` on the development machine. This file is ignored by Git; do not commit it. Confirm its `project_info.project_id` is the staging ID before building or installing the app.
3. In **Authentication → Sign-in method**, enable only the providers the team plans to test: Email/Password and Google. Add the debug signing certificate SHA-1 (and SHA-256 if required by the provider setup) for the local test build. Use dedicated test accounts.
4. Create the default Cloud Firestore database in Native mode. Deploy the reviewed rules to staging only:

   ```powershell
   firebase deploy --project ecell-dcrust-staging --only firestore:rules
   ```

   If staging already has data, take a staging backup and run the contact migration in its default dry-run mode first. Review its report before any explicit apply. A newly created, empty staging project does not need the legacy contact migration.
5. For cloud photo upload, first decide whether the team accepts enabling Blaze billing for this staging project. Firebase requires Blaze to use Cloud Storage for Firebase. If billing is not authorized, do not enable it; use the local Storage emulator and leave cloud upload unverified. If it is authorized, create the staging project's default Storage bucket and deploy the reviewed rules only to staging:

   ```powershell
   firebase deploy --project ecell-dcrust-staging --only storage
   ```

6. Deploy Functions only if staging notifications are part of the pilot and the project is configured for them. Confirm the Firebase CLI's selected project before deployment. Functions are not deployed by the Android build or by local rule tests.
7. Create dedicated coordinator, committee-head, and member test accounts. Sign each account in to create its initial profile. Promote only the staging coordinator using an authorized project administrator, then have that coordinator approve the other test accounts through the app. Never promote a production user for a staging test.
8. Build and install only after the local Firebase config points at the staging project:

   ```powershell
   .\gradlew.bat assembleDebug
   .\gradlew.bat installDebug
   ```

   Before installation, re-check `app/google-services.json`'s project ID. The app currently has one Android application ID and does not yet have separate Gradle product flavors for staging and production.

## Onboarding and member ID pilot checks

- Sign up with a new test account. Confirm it opens the five-step profile setup and that closing/reopening the app resumes the saved draft.
- Enter a name and university email, phone (optional by default), branch, academic year, optional section and enrollment number, optional committee preference, and optional introduction.
- Use the system Photo Picker or camera. The app decodes the selected image, center-crops it to a square, resizes it to 512×512, compresses it as JPEG up to 350 KB, and uploads to the signed-in member's `memberPhotos/{uid}/profile.jpg` path. There is no interactive crop-adjustment control yet. A photo is optional.
- Submit and verify the member waits for coordinator review. Test requesting correction and resubmitting, and rejection followed by reapplication.
- As coordinator, review the photo and private academic/contact fields, select a committee and allowed role, then approve. Approval assigns a stable `EC-` member ID and creates a private verification index and audit entry.
- As the approved member, open the E-Cell Member ID, save/share its image, and scan its QR using the coordinator verifier. The QR contains only `ECELL-ID:<member ID>`; verification returns name, branch, year, assigned committee and role. It is an E-Cell membership card, not a DCRUST university ID.
- Verify that pending/rejected members cannot access an approved-looking card and that unrelated members cannot read enrollment/contact fields or pending profile photos.
- Test a denied upload, network loss, coordinator correction, retry, logout, and a later sign-in.

## Local emulator checks

The repository's rules suite starts Firestore and Storage emulators on ports 8080 and 9199, runs authorized and denied access cases, then shuts them down:

```powershell
cd firestore-tests
npm ci
npm test
```

This suite uses the reserved `demo-ecell-dcrust` project ID and does not connect to Firebase production. It verifies rules, not the Android user interface or a full account lifecycle.

## Pilot checklist and feedback template

Use separate test accounts for coordinator, committee head and member. Keep the pilot to 5–10 invited members and use sample data only. Before inviting members, verify that the installed build points to staging and that staging rules have been deployed.

| Journey | Result / notes |
| --- | --- |
| Sign up, save draft, close, resume | |
| Submit profile and wait for review | |
| Coordinator requests correction; member resubmits | |
| Coordinator rejects; member reapplies | |
| Approve and assign committee/role | |
| Upload, replace and view profile photo | |
| Open, export and verify Member ID QR | |
| Browse announcements and events by audience | |
| Submit complaint and view confidential response | |
| Vote once, then verify duplicate vote is denied | |
| Send and receive a one-to-one message | |
| Sign out and sign back in | |
| Accessibility: large font, TalkBack, keyboard and touch targets | |
| Device / Android version / date | |
| Feedback and steps to reproduce | |

## Known limitations before a real pilot

- Staging Firebase project, credentials, rules deployment and real device journey have not been verified.
- Cloud profile-photo uploads will fail until a separate staging bucket is configured. Cloud Storage requires Blaze billing; local emulator tests do not require a cloud bucket.
- The photo crop is automatic and centered, without a user-adjustable crop interface.
- QR verification requires an approved coordinator account and an available Firestore connection; there is no public verification endpoint.
- Push approval notices require the Functions deployment and a registered FCM token. The Android client alone cannot send them.
- Cloud administrators using privileged Firebase/Admin SDK access can read the private application data. The app's access rules do not constrain trusted project administrators.
- Release builds remain unsigned unless all four signing variables are configured with an existing private keystore. No key is generated or stored in this repository.

## Optional profile field policy

Phone and enrollment number are optional by default. To require either field for a local build, pass one or both Gradle properties:

```powershell
.\gradlew.bat assembleDebug -PecellProfilePhoneRequired=true -PecellProfileEnrollmentRequired=true
```

The app validates the configured policy before submission. Keep these policy values aligned with the actual E-Cell membership requirements and the coordinator's expectations.
