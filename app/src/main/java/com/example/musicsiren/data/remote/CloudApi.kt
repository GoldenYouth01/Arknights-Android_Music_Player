package com.example.musicsiren.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/** 自建云端 API（sevencentury.cn，PHP 后端）。统一响应 ApiResponse(code,msg,data)，code=0 成功。 */
interface CloudApi {

    @POST("api/auth/send-code")
    suspend fun sendCode(@Body body: SendCodeReq): ApiResponse<EmptyData>

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterReq): ApiResponse<LoginDataDto>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginReq): ApiResponse<LoginDataDto>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordReq): ApiResponse<EmptyData>

    @GET("api/auth/me")
    suspend fun me(): ApiResponse<UserDto>

    @POST("api/auth/update-nickname")
    suspend fun updateNickname(@Body body: UpdateNicknameReq): ApiResponse<UserDto>

    @Multipart
    @POST("api/auth/upload-avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): ApiResponse<UserDto>

    @GET("api/playlists")
    suspend fun playlists(): ApiResponse<PlaylistsDataDto>

    @POST("api/playlists/sync")
    suspend fun syncPlaylists(@Body body: SyncPlaylistsReq): ApiResponse<EmptyData>

    @DELETE("api/playlists/{id}")
    suspend fun deletePlaylist(@Path("id") id: String): ApiResponse<EmptyData>

    @POST("api/playlists/{id}/share")
    suspend fun share(@Path("id") id: String): ApiResponse<ShareCodeDto>

    @DELETE("api/playlists/{id}/share")
    suspend fun unshare(@Path("id") id: String): ApiResponse<EmptyData>

    @GET("api/share/{code}")
    suspend fun getShare(@Path("code") code: String): ApiResponse<CloudPlaylistDto>

    @POST("api/share/{code}/save")
    suspend fun saveShare(@Path("code") code: String): ApiResponse<CloudPlaylistDto>

    @GET("api/history")
    suspend fun history(): ApiResponse<HistoryListDto>

    @POST("api/history")
    suspend fun addHistory(@Body body: HistoryAddReq): ApiResponse<EmptyData>
}
