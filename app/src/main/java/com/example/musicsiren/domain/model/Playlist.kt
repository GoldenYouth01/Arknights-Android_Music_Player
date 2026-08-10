package com.example.musicsiren.domain.model

import kotlinx.serialization.Serializable

/** 歌单里的歌曲快照（持久化用，不存 sourceUrl；播放时经 enrich 补齐）。 */
@Serializable
data class PlaylistSong(
    val cid: String,
    val name: String,
    val artists: List<String> = emptyList(),
    val albumCid: String? = null,
    val coverUrl: String? = null,
)

@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val createdAt: Long = 0L,
    val coverUrl: String? = null,
    val songs: List<PlaylistSong> = emptyList(),
    /** 云端同步标记：非空表示已上传，值即云端 id（=本地 id，全量上传/下载约定）。 */
    val cloudId: String? = null,
)
