package com.genciptv.player.data.source.local.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.genciptv.player.data.model.SubtitleEdgeType
import com.genciptv.player.data.model.SubtitleFontFamily
import com.genciptv.player.data.model.SubtitleFontStyle
import com.genciptv.player.data.model.SubtitleStyle
import com.genciptv.player.data.model.SubtitleVerticalPosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubtitleStyleDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    val flow: Flow<SubtitleStyle> = dataStore.data.map { p ->
        SubtitleStyle(
            fontFamily = runCatching {
                SubtitleFontFamily.valueOf(p[KEY_FONT_FAMILY] ?: SubtitleFontFamily.SANS_SERIF.name)
            }.getOrDefault(SubtitleFontFamily.SANS_SERIF),
            fontStyle = runCatching {
                SubtitleFontStyle.valueOf(p[KEY_FONT_STYLE] ?: SubtitleFontStyle.NORMAL.name)
            }.getOrDefault(SubtitleFontStyle.NORMAL),
            textSizePercent = p[KEY_TEXT_SIZE] ?: 100,
            textColor = p[KEY_TEXT_COLOR] ?: 0xFFFFFFFF.toInt(),
            backgroundColor = p[KEY_BG_COLOR] ?: 0x80000000.toInt(),
            windowColor = p[KEY_WINDOW_COLOR] ?: 0x00000000,
            edgeType = runCatching {
                SubtitleEdgeType.valueOf(p[KEY_EDGE_TYPE] ?: SubtitleEdgeType.OUTLINE.name)
            }.getOrDefault(SubtitleEdgeType.OUTLINE),
            edgeColor = p[KEY_EDGE_COLOR] ?: 0xFF000000.toInt(),
            verticalPosition = runCatching {
                SubtitleVerticalPosition.valueOf(
                    p[KEY_VERTICAL_POS] ?: SubtitleVerticalPosition.BOTTOM.name,
                )
            }.getOrDefault(SubtitleVerticalPosition.BOTTOM),
        )
    }

    suspend fun update(style: SubtitleStyle) {
        dataStore.edit { p ->
            p[KEY_FONT_FAMILY] = style.fontFamily.name
            p[KEY_FONT_STYLE] = style.fontStyle.name
            p[KEY_TEXT_SIZE] = style.textSizePercent
            p[KEY_TEXT_COLOR] = style.textColor
            p[KEY_BG_COLOR] = style.backgroundColor
            p[KEY_WINDOW_COLOR] = style.windowColor
            p[KEY_EDGE_TYPE] = style.edgeType.name
            p[KEY_EDGE_COLOR] = style.edgeColor
            p[KEY_VERTICAL_POS] = style.verticalPosition.name
        }
    }

    suspend fun reset() = update(SubtitleStyle.Default)

    companion object {
        const val FILE = "subtitle_prefs"
        private val KEY_FONT_FAMILY = stringPreferencesKey("font_family")
        private val KEY_FONT_STYLE = stringPreferencesKey("font_style")
        private val KEY_TEXT_SIZE = intPreferencesKey("text_size")
        private val KEY_TEXT_COLOR = intPreferencesKey("text_color")
        private val KEY_BG_COLOR = intPreferencesKey("bg_color")
        private val KEY_WINDOW_COLOR = intPreferencesKey("window_color")
        private val KEY_EDGE_TYPE = stringPreferencesKey("edge_type")
        private val KEY_EDGE_COLOR = intPreferencesKey("edge_color")
        private val KEY_VERTICAL_POS = stringPreferencesKey("vertical_pos")
    }
}
