package com.example.musicsiren.data.remote

import kotlinx.serialization.Serializable

/** 无内容端点的 data:{} 占位（kotlinx-serialization 不支持 Unit）。 */
@Serializable
object EmptyData

// ---- Auth 请求 / 响应 ----

@Serializable
data class SendCodeReq(
    val email: String,
    val purpose: String, // register | reset
)

@Serializable
data class RegisterReq(
    val email: String,
    val passwordHash: String,
    val code: String,
    val nickname: String? = null,
)

@Serializable
data class LoginReq(
    val email: String,
    val passwordHash: String,
)

@Serializable
data class ResetPasswordReq(
    val email: String,
    val code: String,
    val passwordHash: String,
)

@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    val nickname: String? = null,
    val avatarUrl: String? = null,
)

@Serializable
data class UpdateNicknameReq(
    val nickname: String,
)

@Serializable
data class LoginDataDto(
    val token: String,
    val user: UserDto,
)

// ---- Playlists / Share ----

@Serializable
data class CloudSongDto(
    val cid: String,
    val name: String,
    val artists: List<String> = emptyList(),
    val albumCid: String? = null,
    val coverUrl: String? = null,
)

@Serializable
data class CloudPlaylistDto(
    val id: String,
    val name: String,
    val createdAt: Long = 0L,
    val coverUrl: String? = null,
    val shareCode: String? = null,
    val songs: List<CloudSongDto> = emptyList(),
)

@Serializable
data class PlaylistsDataDto(
    val playlists: List<CloudPlaylistDto> = emptyList(),
)

@Serializable
data class SyncPlaylistsReq(
    val playlists: List<CloudPlaylistDto>,
)

@Serializable
data class ShareCodeDto(
    val code: String,
)

// ---- History ----

@Serializable
data class HistoryAddReq(
    val cid: String,
    val songName: String,
    val artists: List<String> = emptyList(),
    val albumCid: String? = null,
    val coverUrl: String? = null,
    val playedAt: Long = 0L,
)

@Serializable
data class HistoryItemDto(
    val cid: String,
    val songName: String,
    val artists: List<String> = emptyList(),
    val albumCid: String? = null,
    val coverUrl: String? = null,
    val playedAt: Long = 0L,
)

@Serializable
data class HistoryListDto(
    val list: List<HistoryItemDto> = emptyList(),
)
