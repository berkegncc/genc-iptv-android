# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ── Release logging ────────────────────────────────────────────────────────────
# Strip verbose/debug/info logging from release builds. These carry stream URLs,
# sync counts and diagnostics that are useful while developing and are a data
# leak in a shipped app — logcat is readable via `adb` and lands in bug reports,
# and Xtream endpoints embed the subscription username and password.
#
# Warnings and errors stay: they are what makes a crash report diagnosable.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ── Kotlin ─────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ── kotlinx.serialization ──────────────────────────────────────────────────────
# Scoped to the `dto` package under every remote source (xtream, tmdb, github)
# rather than all of `com.genciptv.player.**`, so the rest of the app still
# shrinks and obfuscates normally. Was xtream-only, which left the tmdb and
# github DTOs to be stripped in release builds.
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
-keep,includedescriptorclasses class com.genciptv.player.data.source.**.dto.**$$serializer { *; }
-keepclassmembers class com.genciptv.player.data.source.**.dto.** {
    *** Companion;
}
-keepclasseswithmembers class com.genciptv.player.data.source.**.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Hilt ───────────────────────────────────────────────────────────────────────
-keep,allowobfuscation,allowshrinking class dagger.hilt.** { *; }
-keep,allowobfuscation,allowshrinking class javax.inject.** { *; }

# ── Room ───────────────────────────────────────────────────────────────────────
-keep class androidx.room.** { *; }
-keep class com.genciptv.player.data.source.local.entity.** { *; }

# ── Retrofit + OkHttp ──────────────────────────────────────────────────────────
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# ── Media3 / ExoPlayer ─────────────────────────────────────────────────────────
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ── Coil ───────────────────────────────────────────────────────────────────────
-dontwarn coil3.**

# ── DataStore ──────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ── WorkManager ────────────────────────────────────────────────────────────────
-keep class androidx.work.** { *; }
-keep class com.genciptv.player.data.worker.** { *; }
