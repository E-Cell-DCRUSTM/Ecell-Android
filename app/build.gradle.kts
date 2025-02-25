plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "dcrustm.ecell.mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "dcrustm.ecell.mobile"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    ksp("com.google.dagger:hilt-compiler:2.55")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Work manager dependencies
    implementation("androidx.work:work-runtime:2.10.0")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.hilt:hilt-work:1.2.0")

    // Room dependencies
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    //  Navigation compose library
    implementation("androidx.navigation:navigation-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Datepicker and time picker dialog 3rd party dependencies
    implementation("com.maxkeppeler.sheets-compose-dialogs:core:1.3.0")
    implementation("com.maxkeppeler.sheets-compose-dialogs:clock:1.3.0")
    implementation("com.maxkeppeler.sheets-compose-dialogs:duration:1.3.0")
    implementation("com.maxkeppeler.sheets-compose-dialogs:calendar:1.3.0")

    // Accompanist Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.37.0")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.36.0")

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


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}