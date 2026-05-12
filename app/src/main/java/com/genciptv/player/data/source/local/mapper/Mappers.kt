package com.genciptv.player.data.source.local.mapper

import com.genciptv.player.data.model.Channel
import com.genciptv.player.data.model.ContinueWatching
import com.genciptv.player.data.model.Episode
import com.genciptv.player.data.model.Favorite
import com.genciptv.player.data.model.FavoriteTargetType
import com.genciptv.player.data.model.Playlist
import com.genciptv.player.data.model.PlaylistType
import com.genciptv.player.data.model.Program
import com.genciptv.player.data.model.Series
import com.genciptv.player.data.model.VodCategory
import com.genciptv.player.data.model.VodItem
import com.genciptv.player.data.model.VodKind
import com.genciptv.player.data.model.XtreamUserInfo
import com.genciptv.player.data.source.local.entity.ChannelEntity
import com.genciptv.player.data.source.local.entity.ContinueWatchingEntity
import com.genciptv.player.data.source.local.entity.EpisodeEntity
import com.genciptv.player.data.source.local.entity.FavoriteEntity
import com.genciptv.player.data.source.local.entity.PlaylistEntity
import com.genciptv.player.data.source.local.entity.ProgramEntity
import com.genciptv.player.data.source.local.entity.SeriesEntity
import com.genciptv.player.data.source.local.entity.VodCategoryEntity
import com.genciptv.player.data.source.local.entity.VodEntity
import com.genciptv.player.data.source.local.entity.XtreamUserInfoEmbedded

// ── Playlist ──────────────────────────────────────────────────────────────────

fun PlaylistEntity.toDomain(): Playlist = Playlist(
    id = id,
    name = name,
    type = runCatching { PlaylistType.valueOf(type) }.getOrDefault(PlaylistType.M3U),
    url = url,
    username = username,
    password = password,
    epgUrl = epgUrl,
    isActive = isActive,
    lastSyncedAt = lastSyncedAt,
    channelCount = channelCount,
    userAgent = userAgent,
    userInfo = xtreamUserInfo?.let { e ->
        if (e.username == null) null
        else XtreamUserInfo(
            username = e.username,
            status = e.status ?: "Unknown",
            expDateMillis = e.expDateMillis,
            isTrial = e.isTrial ?: false,
            maxConnections = e.maxConnections,
        )
    },
)

fun Playlist.toEntity(): PlaylistEntity = PlaylistEntity(
    id = id,
    name = name,
    type = type.name,
    url = url,
    username = username,
    password = password,
    epgUrl = epgUrl,
    isActive = isActive,
    lastSyncedAt = lastSyncedAt,
    channelCount = channelCount,
    userAgent = userAgent,
    xtreamUserInfo = userInfo?.let {
        XtreamUserInfoEmbedded(
            username = it.username,
            status = it.status,
            expDateMillis = it.expDateMillis,
            isTrial = it.isTrial,
            maxConnections = it.maxConnections,
        )
    },
)

// ── Channel ───────────────────────────────────────────────────────────────────

fun ChannelEntity.toDomain(): Channel = Channel(
    id = id,
    playlistId = playlistId,
    name = name,
    logoUrl = logoUrl,
    streamUrl = streamUrl,
    groupTitle = groupTitle,
    epgChannelId = epgChannelId,
    isHd = isHd,
    sortOrder = sortOrder,
    groupSortOrder = groupSortOrder,
)

fun Channel.toEntity(): ChannelEntity = ChannelEntity(
    id = id,
    playlistId = playlistId,
    name = name,
    logoUrl = logoUrl,
    streamUrl = streamUrl,
    groupTitle = groupTitle,
    epgChannelId = epgChannelId,
    isHd = isHd,
    sortOrder = sortOrder,
    groupSortOrder = groupSortOrder,
)

// ── Program ───────────────────────────────────────────────────────────────────

fun ProgramEntity.toDomain(): Program = Program(
    id = id,
    channelEpgId = channelEpgId,
    playlistId = playlistId,
    title = title,
    description = description,
    startMillis = startMillis,
    stopMillis = stopMillis,
    category = category,
)

fun Program.toEntity(): ProgramEntity = ProgramEntity(
    id = id,
    channelEpgId = channelEpgId,
    playlistId = playlistId,
    title = title,
    description = description,
    startMillis = startMillis,
    stopMillis = stopMillis,
    category = category,
)

// ── VOD ───────────────────────────────────────────────────────────────────────

fun VodEntity.toDomain(): VodItem = VodItem(
    id = id,
    playlistId = playlistId,
    title = title,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    streamUrl = streamUrl,
    kind = runCatching { VodKind.valueOf(kind) }.getOrDefault(VodKind.MOVIE),
    year = year,
    rating = rating,
    plot = plot,
    genres = genres,
    cast = cast,
    director = director,
    durationSecs = durationSecs,
    categoryId = categoryId,
)

fun VodItem.toEntity(): VodEntity = VodEntity(
    id = id,
    playlistId = playlistId,
    title = title,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    streamUrl = streamUrl,
    kind = kind.name,
    year = year,
    rating = rating,
    plot = plot,
    genres = genres,
    cast = cast,
    director = director,
    durationSecs = durationSecs,
    categoryId = categoryId,
)

fun SeriesEntity.toDomain(): Series = Series(
    id = id,
    playlistId = playlistId,
    title = title,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    plot = plot,
    year = year,
    rating = rating,
    genres = genres,
    cast = cast,
    categoryId = categoryId,
)

fun Series.toEntity(): SeriesEntity = SeriesEntity(
    id = id,
    playlistId = playlistId,
    title = title,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    plot = plot,
    year = year,
    rating = rating,
    genres = genres,
    cast = cast,
    categoryId = categoryId,
)

fun EpisodeEntity.toDomain(): Episode = Episode(
    id = id,
    seriesId = seriesId,
    playlistId = playlistId,
    season = season,
    episode = episode,
    title = title,
    streamUrl = streamUrl,
    durationSecs = durationSecs,
    plot = plot,
    thumbnailUrl = thumbnailUrl,
)

fun Episode.toEntity(): EpisodeEntity = EpisodeEntity(
    id = id,
    seriesId = seriesId,
    playlistId = playlistId,
    season = season,
    episode = episode,
    title = title,
    streamUrl = streamUrl,
    durationSecs = durationSecs,
    plot = plot,
    thumbnailUrl = thumbnailUrl,
)

fun VodCategoryEntity.toDomain(): VodCategory = VodCategory(
    id = id,
    playlistId = playlistId,
    name = name,
    kind = runCatching { VodKind.valueOf(kind) }.getOrDefault(VodKind.MOVIE),
)

fun VodCategory.toEntity(): VodCategoryEntity = VodCategoryEntity(
    id = id,
    playlistId = playlistId,
    name = name,
    kind = kind.name,
)

// ── Favorite / ContinueWatching ───────────────────────────────────────────────

fun FavoriteEntity.toDomain(): Favorite = Favorite(
    targetId = targetId,
    targetType = runCatching { FavoriteTargetType.valueOf(targetType) }
        .getOrDefault(FavoriteTargetType.CHANNEL),
    addedAt = addedAt,
)

fun Favorite.toEntity(): FavoriteEntity = FavoriteEntity(
    targetId = targetId,
    targetType = targetType.name,
    addedAt = addedAt,
)

fun ContinueWatchingEntity.toDomain(): ContinueWatching = ContinueWatching(
    targetId = targetId,
    targetType = runCatching { FavoriteTargetType.valueOf(targetType) }
        .getOrDefault(FavoriteTargetType.CHANNEL),
    positionMs = positionMs,
    durationMs = durationMs,
    updatedAt = updatedAt,
    title = title,
    subtitle = subtitle,
    thumbnailUrl = thumbnailUrl,
    resumeEpisodeId = resumeEpisodeId,
)

fun ContinueWatching.toEntity(): ContinueWatchingEntity = ContinueWatchingEntity(
    targetId = targetId,
    targetType = targetType.name,
    positionMs = positionMs,
    durationMs = durationMs,
    updatedAt = updatedAt,
    title = title,
    subtitle = subtitle,
    thumbnailUrl = thumbnailUrl,
    resumeEpisodeId = resumeEpisodeId,
)
