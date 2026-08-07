package com.example.musicsiren.ui.screens

import androidx.lifecycle.ViewModel
import com.example.musicsiren.data.repository.PlaylistRepository
import com.example.musicsiren.domain.model.Playlist
import kotlinx.coroutines.flow.StateFlow

class PlaylistsViewModel(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    val playlists: StateFlow<List<Playlist>> = playlistRepository.playlists

    fun createPlaylist(name: String) {
        playlistRepository.createPlaylist(name)
    }

    fun deletePlaylist(id: String) {
        playlistRepository.deletePlaylist(id)
    }
}
