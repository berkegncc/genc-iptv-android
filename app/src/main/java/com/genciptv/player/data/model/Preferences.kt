package com.genciptv.player.data.model

// ── Appearance ────────────────────────────────────────────────────────────────

enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class AppearancePreferences(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    /** Name of AccentPalette enum entry, e.g. "PURPLE", "RED", ... */
    val accentKey: String = "PURPLE",
)

// ── Player ────────────────────────────────────────────────────────────────────

enum class QualityPref { AUTO, P1080, P720, P480 }
enum class DecoderPref { AUTO, HW, SW }

data class PlayerPreferences(
    val defaultQuality: QualityPref = QualityPref.AUTO,
    val preferredAudioLang: String? = null,
    val userAgent: String? = null,
    val decoderPref: DecoderPref = DecoderPref.AUTO,
    val loudnessNormalization: Boolean = false,
    val pictureInPicture: Boolean = true,
)

// ── Subtitles ─────────────────────────────────────────────────────────────────

enum class SubtitleFontFamily { SYSTEM, SANS_SERIF, SERIF, MONOSPACE, CASUAL, CURSIVE }
enum class SubtitleFontStyle { NORMAL, BOLD, ITALIC, BOLD_ITALIC }
enum class SubtitleEdgeType { NONE, OUTLINE, DROP_SHADOW, RAISED, DEPRESSED }
enum class SubtitleVerticalPosition { BOTTOM, CENTER, TOP }

data class SubtitleStyle(
    val fontFamily: SubtitleFontFamily = SubtitleFontFamily.SANS_SERIF,
    val fontStyle: SubtitleFontStyle = SubtitleFontStyle.NORMAL,
    /** 50..200, percent relative to player default size. */
    val textSizePercent: Int = 100,
    /** ARGB packed int. */
    val textColor: Int = 0xFFFFFFFF.toInt(),
    val backgroundColor: Int = 0x80000000.toInt(),
    val windowColor: Int = 0x00000000,
    val edgeType: SubtitleEdgeType = SubtitleEdgeType.OUTLINE,
    val edgeColor: Int = 0xFF000000.toInt(),
    val verticalPosition: SubtitleVerticalPosition = SubtitleVerticalPosition.BOTTOM,
) {
    companion object { val Default = SubtitleStyle() }
}

// ── User ──────────────────────────────────────────────────────────────────────

data class UserPreferences(
    val displayName: String = "",
    val onboardingCompleted: Boolean = false,
    val activePlaylistId: Long = -1L,
    val autoUpdateEnabled: Boolean = true,
)
