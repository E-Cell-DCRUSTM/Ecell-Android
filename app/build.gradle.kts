import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

val releaseStorePath = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
val releaseStorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull
val releaseSigningValues = listOf(
    releaseStorePath, releaseStorePassword, releaseKeyAlias, releaseKeyPassword
)
val releaseSigningConfigured = releaseSigningValues.all { !it.isNullOrBlank() }
if (releaseSigningValues.any { !it.isNullOrBlank() } && !releaseSigningConfigured) {
    throw GradleException("Set all four ANDROID_KEYSTORE/ANDROID_KEY environment variables to configure release signing.")
}
fun configuredBooleanProperty(name: String): Boolean = providers.gradleProperty(name).orNull?.let { value ->
    value.toBooleanStrictOrNull()
        ?: throw GradleException("Gradle property '$name' must be either true or false.")
} ?: false

val profilePhoneRequired = configuredBooleanProperty("ecellProfilePhoneRequired")
val profileEnrollmentRequired = configuredBooleanProperty("ecellProfileEnrollmentRequired")

android {
    namespace = "dcrustm.ecell.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "dcrustm.ecell.mobile"
        minSdk = 29
        targetSdk = 36
        versionCode = 3
        versionName = "1.2.0"
        buildConfigField("boolean", "PROFILE_PHONE_REQUIRED", profilePhoneRequired.toString())
        buildConfigField("boolean", "PROFILE_ENROLLMENT_REQUIRED", profileEnrollmentRequired.toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (releaseSigningConfigured) {
                storeFile = file(releaseStorePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (releaseSigningConfigured) signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Google font provider
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.8")

    // Coroutine dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    // Hilt dependencies
    implementation("com.google.dagger:hilt-android:2.55")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation(libs.androidx.storage)
    implementation(libs.firebase.crashlytics.buildtools)
    ksp("com.google.dagger:hilt-compiler:2.55")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    //  Navigation compose library
    implementation("androidx.navigation:navigation-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Material3 dependencies
    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")

    // datastore preferences components
    implementation("androidx.datastore:datastore-preferences:1.1.2")

    // Kotlinx serialization library
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    // Pager library
    implementation("com.google.accompanist:accompanist-pager:0.22.0-rc")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.22.0-rc")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))

    // Firebase Authentication (for Google Sign-In)
    implementation("com.google.firebase:firebase-auth")

    // Firebase Firestore (app data: members, announcements, complaints)
    implementation("com.google.firebase:firebase-firestore")

    // Firebase Cloud Messaging (push notifications)
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-storage")

    // QR rendering for the private, coordinator-verifiable member ID.
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")

    // Google Sign-In SDK
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
