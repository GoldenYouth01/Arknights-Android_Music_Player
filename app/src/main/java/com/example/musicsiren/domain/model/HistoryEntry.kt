package com.example.musicsiren.domain.model

import kotlinx.serialization.Serializable

/** 播放历史条目（本地/云端共用快照，不存 sourceUrl）。 */
@Serializable
data class HistoryEntry(
    val cid: String,
    val name: String,
    val artists: List<String> = emptyList(),
    val albumCid: String? = null,
    val coverUrl: String? = null,
    val playedAt: Long = 0L,
)
