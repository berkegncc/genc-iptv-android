package com.genciptv.player.feature.profile

import com.genciptv.player.data.model.Playlist
import com.genciptv.player.data.model.PlayerPreferences
import com.genciptv.player.data.model.UserPreferences

data class ProfileUiState(
    val user: UserPreferences = UserPreferences(),
    val player: PlayerPreferences = PlayerPreferences(),
    val activePlaylist: Playlist? = null,
    val playlistCount: Int = 0,
    val planText: String = "—",
)
