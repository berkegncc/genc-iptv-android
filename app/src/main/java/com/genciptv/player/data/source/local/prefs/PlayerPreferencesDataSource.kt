package com.genciptv.player.data.source.local.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.genciptv.player.data.model.DecoderPref
import com.genciptv.player.data.model.PlayerPreferences
import com.genciptv.player.data.model.QualityPref
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlayerPreferencesDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    val flow: Flow<PlayerPreferences> = dataStore.data.map { p ->
        PlayerPreferences(
            defaultQuality = runCatching {
                QualityPref.valueOf(p[KEY_QUALITY] ?: QualityPref.AUTO.name)
            }.getOrDefault(QualityPref.AUTO),
            preferredAudioLang = p[KEY_AUDIO_LANG],
            userAgent = p[KEY_USER_AGENT],
            decoderPref = runCatching {
                DecoderPref.valueOf(p[KEY_DECODER] ?: DecoderPref.AUTO.name)
            }.getOrDefault(DecoderPref.AUTO),
            trustAllCerts = p[KEY_TRUST_ALL] ?: false,
            loudnessNormalization = p[KEY_LOUDNESS] ?: false,
            pictureInPicture = p[KEY_PIP] ?: true,
        )
    }

    suspend fun update(block: (PlayerPreferences) -> PlayerPreferences) {
        dataStore.edit { prefs ->
            val current = PlayerPreferences(
                defaultQuality = runCatching {
                    QualityPref.valueOf(prefs[KEY_QUALITY] ?: QualityPref.AUTO.name)
                }.getOrDefault(QualityPref.AUTO),
                preferredAudioLang = prefs[KEY_AUDIO_LANG],
                userAgent = prefs[KEY_USER_AGENT],
                decoderPref = runCatching {
                    DecoderPref.valueOf(prefs[KEY_DECODER] ?: DecoderPref.AUTO.name)
                }.getOrDefault(DecoderPref.AUTO),
                trustAllCerts = prefs[KEY_TRUST_ALL] ?: false,
                loudnessNormalization = prefs[KEY_LOUDNESS] ?: false,
                pictureInPicture = prefs[KEY_PIP] ?: true,
            )
            val updated = block(current)
            prefs[KEY_QUALITY] = updated.defaultQuality.name
            updated.preferredAudioLang?.let { prefs[KEY_AUDIO_LANG] = it }
                ?: prefs.remove(KEY_AUDIO_LANG)
            updated.userAgent?.let { prefs[KEY_USER_AGENT] = it }
                ?: prefs.remove(KEY_USER_AGENT)
            prefs[KEY_DECODER] = updated.decoderPref.name
            prefs[KEY_TRUST_ALL] = updated.trustAllCerts
            prefs[KEY_LOUDNESS] = updated.loudnessNormalization
            prefs[KEY_PIP] = updated.pictureInPicture
        }
    }

    companion object {
        const val FILE = "player_prefs"
        private val KEY_QUALITY = stringPreferencesKey("default_quality")
        private val KEY_AUDIO_LANG = stringPreferencesKey("audio_lang")
        private val KEY_USER_AGENT = stringPreferencesKey("user_agent")
        private val KEY_DECODER = stringPreferencesKey("decoder")
        private val KEY_TRUST_ALL = booleanPreferencesKey("trust_all_certs")
        private val KEY_LOUDNESS = booleanPreferencesKey("loudness_norm")
        private val KEY_PIP = booleanPreferencesKey("pip")
    }
}
