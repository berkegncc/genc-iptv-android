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

# ── Kotlin ─────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ── kotlinx.serialization ──────────────────────────────────────────────────────
# Narrowed to the only package that actually hosts @Serializable DTOs — the
# rest of `com.genciptv.player.**` is now free to shrink/obfuscate normally.
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
-keep,includedescriptorclasses class com.genciptv.player.data.source.xtream.dto.**$$serializer { *; }
-keepclassmembers class com.genciptv.player.data.source.xtream.dto.** {
    *** Companion;
}
-keepclasseswithmembers class com.genciptv.player.data.source.xtream.dto.** {
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
