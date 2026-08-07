package com.example.musicsiren.ui.screens

import androidx.lifecycle.ViewModel
import com.example.musicsiren.data.repository.PlaylistRepository
import com.example.musicsiren.domain.model.Playlist
import com.example.musicsiren.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

class PlaylistDetailViewModel(
    private val playlistRepository: PlaylistRepository,
    private val playlistId: String,
) : ViewModel() {

    val playlists: StateFlow<List<Playlist>> = playlistRepository.playlists

    val playlist: Playlist?
        get() = playlistRepository.playlist(playlistId)

    fun removeSong(cid: String) = playlistRepository.removeSong(playlistId, cid)
    fun rename(name: String) = playlistRepository.renamePlaylist(playlistId, name)
    fun delete() = playlistRepository.deletePlaylist(playlistId)

    /** 歌单歌曲 → 播放用领域 Song（sourceUrl 为空，由播放层 enrich 补齐）。 */
    fun toSongs(): List<Song> =
        playlist?.songs?.map {
            Song(cid = it.cid, name = it.name, albumCid = it.albumCid, artists = it.artists, sourceUrl = null, lyricUrl = null)
        } ?: emptyList()
}
