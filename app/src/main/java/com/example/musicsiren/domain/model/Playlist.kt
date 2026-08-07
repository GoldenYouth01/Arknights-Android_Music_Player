package com.example.musicsiren.domain.model

import kotlinx.serialization.Serializable

/** 歌单里的歌曲快照（持久化用，不存 sourceUrl；播放时经 enrich 补齐）。 */
@Serializable
data class PlaylistSong(
    val cid: String,
    val name: String,
    val artists: List<String> = emptyList(),
    val albumCid: String? = null,
)

@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val createdAt: Long = 0L,
    val coverUrl: String? = null,
    val songs: List<PlaylistSong> = emptyList(),
)
