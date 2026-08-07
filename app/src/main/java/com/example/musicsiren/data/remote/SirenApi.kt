package com.example.musicsiren.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SirenApi {
    @GET("api/albums")
    suspend fun albums(): ApiResponse<List<AlbumDto>>

    @GET("api/album/{cid}/detail")
    suspend fun albumDetail(@Path("cid") cid: String): ApiResponse<AlbumDetailDto>

    @GET("api/song/{cid}")
    suspend fun song(@Path("cid") cid: String): ApiResponse<SongDto>

    @GET("api/songs")
    suspend fun songs(): ApiResponse<SongsDataDto>

    @GET("api/search")
    suspend fun search(@Query("keyword") keyword: String): ApiResponse<SearchDataDto>
}
