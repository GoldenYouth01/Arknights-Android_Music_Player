package com.example.musicsiren.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    DOWNLOADED,
    ERROR,
}

/** 一条离线下载记录，持久化在 DataStore（免 Room/KSP）。 */
@Serializable
data class DownloadRecord(
    val songCid: String,
    val songName: String,
    val artistNames: List<String> = emptyList(),
    val albumCid: String = "",
    val albumName: String = "",
    val coverUrl: String? = null,
    val sourceUrl: String = "",
    val localPath: String? = null,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val progressBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val error: String? = null,
    val completedAt: Long = 0L,
)
