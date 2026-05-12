package com.genciptv.player.feature.home.model

data class FeaturedItem(
    val channelId: String,
    /** Program title if available, else channel name. */
    val title: String,
    /** "{channelName} · {time}" */
    val subtitle: String,
    /** 0..4 — index into the 5 gradient presets matching HTML .feat-bg t1..t5. */
    val gradientBrushKey: Int,
)
