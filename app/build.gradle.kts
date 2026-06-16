import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

// Odczyt local.properties — klucze API nie trafiają do repo
val localProps = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

android {
    namespace = "wat.edu.pl.projektam"
    compileSdk = 35

    defaultConfig {
        applicationId = "wat.edu.pl.projektam"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Przekazuje klucz do AndroidManifest.xml jako ${MAPS_API_KEY}
        manifestPlaceholders["MAPS_API_KEY"] = localProps.getProperty("MAPS_API_KEY", "")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // ── AndroidX Core ──────────────────────────────────────────────
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")

    // ── Material Design 3 ──────────────────────────────────────────
    implementation("com.google.android.material:material:1.12.0")

    // ── Lifecycle (ViewModel + LiveData) ────────────────────────────
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // ── Navigation Component ───────────────────────────────────────
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")

    // ── Hilt (Dependency Injection) ────────────────────────────────
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")

    // ── Room (Local Database) ──────────────────────────────────────
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ── Retrofit + OkHttp (Network) ────────────────────────────────
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ── Google Maps + Location ─────────────────────────────────────
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // ── Security (EncryptedSharedPreferences) ──────────────────────
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ── DataStore Preferences (settings persistence) ───────────────
    implementation("androidx.datastore:datastore-preferences:1.1.2")

    // ── Kotlin Coroutines ──────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // ── Testing ────────────────────────────────────────────────────
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
