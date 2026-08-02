import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
}

// Read secrets from local.properties (gitignored). TMDB_API_KEY=xxx for actor photos.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) FileInputStream(file).use { load(it) }
}
// Env var takes precedence so CI / public release builds can ship an APK
// without baking the maintainer's TMDB key into the bytecode. Local dev
// keeps reading from `local.properties` as before.
val tmdbApiKey: String = System.getenv("TMDB_API_KEY")
    ?: localProperties.getProperty("TMDB_API_KEY", "")

// Release signing secrets. keystore.properties is gitignored; env vars take
// precedence so CI can sign without a file on disk. Fall back to debug if absent.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) FileInputStream(file).use { load(it) }
}
fun signingProp(key: String, env: String): String? =
    System.getenv(env) ?: keystoreProperties.getProperty(key)
val releaseStoreFile = signingProp("storeFile", "RELEASE_STORE_FILE")
val hasReleaseSigning = releaseStoreFile != null && rootProject.file(releaseStoreFile).exists()

// A fresh clone has no keystore and still needs to produce something
// installable, so `release` falls back to the debug key (see buildTypes). But
// once the project *is* configured for release signing, a fallback means a
// typo'd path or a moved keystore — and a debug-signed "release" APK installs
// for nobody who already has the app, with no visible symptom until users
// report it. Fail the release build instead of shipping that.
val wantsReleaseSigning =
    !keystoreProperties.isEmpty || System.getenv("RELEASE_STORE_FILE") != null

gradle.taskGraph.whenReady {
    val buildingRelease = allTasks.any { it.name.endsWith("Release") }
    if (buildingRelease && wantsReleaseSigning && !hasReleaseSigning) {
        throw GradleException(
            "Release signing is configured but the keystore was not found at " +
                "'${releaseStoreFile ?: "<unset>"}'. This build would fall back to the " +
                "debug key and produce an APK that no existing install can accept as an " +
                "update. Fix keystore.properties (or RELEASE_STORE_FILE) and rebuild."
        )
    }
    // Warn, don't fail: a baked-in key is exactly what you want for local
    // testing, and only wrong in an APK you hand to other people. Anyone who
    // downloads it can extract the key, and every install then shares one
    // quota — users set their own under Profil → Hakkında instead.
    if (buildingRelease && tmdbApiKey.isNotBlank()) {
        // ASCII only: Gradle writes UTF-8 but Windows consoles still default to
        // a legacy code page, and a warning nobody can read is no warning.
        logger.warn(
            "\n" +
                "==============================================================\n" +
                "  WARNING: this release APK has a TMDB API key compiled in.\n" +
                "  Fine for local testing, not for anything you publish -\n" +
                "  anyone who downloads the APK can extract it.\n" +
                "  Clear TMDB_API_KEY in local.properties and rebuild before\n" +
                "  uploading. Users supply their own key in the app instead.\n" +
                "=============================================================="
        )
    }
}

android {
    namespace = "com.genciptv.player"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.genciptv.player"
        minSdk = 24
        targetSdk = 36
        // Bump on every release — Android decides "is this an update?" from
        // this number, not from versionName. 3 was consumed by update-system
        // testing on a device, so the first published build after v1.1 is 4.
        versionCode = 4
        // Three-part semver, and the release tag is `v` + this exact string
        // (v1.2.0) — the in-app updater compares them directly.
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile!!)
                storePassword = signingProp("storePassword", "RELEASE_STORE_PASSWORD")
                keyAlias = signingProp("keyAlias", "RELEASE_KEY_ALIAS")
                keyPassword = signingProp("keyPassword", "RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Sign with the real release keystore when keystore.properties / env vars
            // are present. Otherwise fall back to the debug key so a plain local
            // assembleRelease still produces an installable APK. NEVER ship debug-signed.
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
        unitTests.all {
            it.jvmArgs(
                "-Dfile.encoding=UTF-8",
                "-Dsun.jnu.encoding=UTF-8",
            )
        }
    }
}

// Room schema export directory (must be outside android{} block)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Hilt aggregating task optimisation
hilt {
    enableAggregatingTask = true
}

dependencies {
    // ── Core AndroidX ────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // ── Navigation ────────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ── Compose ───────────────────────────────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.windowsize)
    implementation(libs.compose.ui.text.google.fonts)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)

    // ── Hilt DI ───────────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // ── Room ──────────────────────────────────────────────────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ── Media3 / ExoPlayer ────────────────────────────────────────────────────
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.ui)
    implementation(libs.media3.session)
    implementation(libs.media3.common)

    // ── Networking ────────────────────────────────────────────────────────────
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // ── DataStore ─────────────────────────────────────────────────────────────
    implementation(libs.datastore.preferences)

    // ── Coil 3 ────────────────────────────────────────────────────────────────
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // ── kotlinx-datetime ──────────────────────────────────────────────────────
    implementation(libs.kotlinx.datetime)

    // ── WorkManager + Hilt ────────────────────────────────────────────────────
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)

    // ── Testing ───────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
