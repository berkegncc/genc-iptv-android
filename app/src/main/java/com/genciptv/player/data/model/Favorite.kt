package com.genciptv.player.data.model

enum class FavoriteTargetType { CHANNEL, MOVIE, SERIES }

data class Favorite(
    val targetId: String,
    val targetType: FavoriteTargetType,
    val addedAt: Long,
)
