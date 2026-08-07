package com.example.musicsiren.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 塞壬唱片 API 统一响应包装：code=0 为成功。 */
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val msg: String? = null,
    val data: T? = null,
)

@Serializable
data class AlbumDto(
    val cid: String,
    val name: String,
    val coverUrl: String? = null,
    val coverDeUrl: String? = null,
    @SerialName("artistes") val artistes: List<String> = emptyList(),
)

/**
 * 歌曲 DTO。字段名坑：专辑详情里的歌曲用 `artistes`，
 * `/api/song` 与 `/api/songs` 用 `artists`。这里同时声明，[artistNames] 优先取非空者。
 */
@Serializable
data class SongDto(
    val cid: String,
    val name: String,
    val albumCid: String? = null,
    val sourceUrl: String? = null,
    val lyricUrl: String? = null,
    @SerialName("artistes") val artistes: List<String> = emptyList(),
    @SerialName("artists") val artists: List<String> = emptyList(),
) {
    fun artistNames(): List<String> = if (artists.isNotEmpty()) artists else artistes
}

@Serializable
data class AlbumDetailDto(
    val cid: String,
    val name: String,
    val coverUrl: String? = null,
    val coverDeUrl: String? = null,
    @SerialName("artistes") val artistes: List<String> = emptyList(),
    val songs: List<SongDto> = emptyList(),
)

@Serializable
data class SongsDataDto(
    val list: List<SongDto> = emptyList(),
    val autoplay: String? = null,
)

@Serializable
data class SearchDataDto(
    val albums: SearchListDto = SearchListDto(),
    val news: SearchListDto = SearchListDto(),
)

@Serializable
data class SearchListDto(
    val list: List<AlbumDto> = emptyList(),
    val end: Boolean = true,
)
