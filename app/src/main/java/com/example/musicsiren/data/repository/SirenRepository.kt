package com.example.musicsiren.data.repository

import com.example.musicsiren.data.remote.AlbumDetailDto
import com.example.musicsiren.data.remote.AlbumDto
import com.example.musicsiren.data.remote.ApiResponse
import com.example.musicsiren.data.remote.SirenApi
import com.example.musicsiren.data.remote.SongDto
import com.example.musicsiren.domain.model.Album
import com.example.musicsiren.domain.model.AlbumDetail
import com.example.musicsiren.domain.model.Song

class SirenApiException(message: String) : Exception(message)

class SirenRepository(private val api: SirenApi) {

    suspend fun getAlbums(): List<Album> =
        api.albums().dataOrThrow().map { it.toDomain() }

    suspend fun getAlbumDetail(cid: String): AlbumDetail {
        val dto = api.albumDetail(cid).dataOrThrow()
        return AlbumDetail(
            album = dto.toAlbum(),
            songs = dto.songs.map { it.toDomain(albumCid = dto.cid) },
        )
    }

    suspend fun getSong(cid: String): Song = api.song(cid).dataOrThrow().toDomain()

    suspend fun getSongUrl(cid: String): String? = api.song(cid).dataOrThrow().sourceUrl

    suspend fun getSongsCatalog(): List<Song> =
        api.songs().dataOrThrow().list.map { it.toDomain() }

    suspend fun searchAlbums(keyword: String): List<Album> =
        api.search(keyword).dataOrThrow().albums.list.map { it.toDomain() }
}

private fun <T> ApiResponse<T>.dataOrThrow(): T {
    if (code != 0) throw SirenApiException("API error code=$code msg=$msg")
    return data ?: throw SirenApiException("API returned null data")
}

private fun AlbumDto.toDomain() = Album(
    cid = cid,
    name = name,
    coverUrl = coverUrl,
    coverDeUrl = coverDeUrl,
    artistes = artistes,
)

private fun AlbumDetailDto.toAlbum() = Album(
    cid = cid,
    name = name,
    coverUrl = coverUrl,
    coverDeUrl = coverDeUrl,
    artistes = artistes,
)

private fun SongDto.toDomain(albumCid: String? = this.albumCid) = Song(
    cid = cid,
    name = name,
    albumCid = albumCid,
    artists = artistNames(),
    sourceUrl = sourceUrl,
    lyricUrl = lyricUrl,
)
