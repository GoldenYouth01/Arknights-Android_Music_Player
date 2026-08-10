package com.example.musicsiren.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicsiren.data.repository.LyricsRepository
import com.example.musicsiren.domain.model.LyricLine
import com.example.musicsiren.domain.model.Song
import com.example.musicsiren.playback.PlaybackViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed interface LyricsUiState {
    data object Idle : LyricsUiState
    data object Loading : LyricsUiState
    data class Lyrics(val lines: List<LyricLine>) : LyricsUiState
    data object NoLyrics : LyricsUiState
}

/** 歌词状态：观察当前播放歌曲，切换时加载歌词；无歌词 → NoLyrics（显示纯音乐）。 */
class LyricsViewModel(
    private val lyricsRepository: LyricsRepository,
    playbackViewModel: PlaybackViewModel,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LyricsUiState>(LyricsUiState.Idle)
    val uiState: StateFlow<LyricsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            playbackViewModel.uiState
                .map { it.currentSong }
                .distinctUntilChanged()
                .collect { song -> loadFor(song) }
        }
    }

    private suspend fun loadFor(song: Song?) {
        if (song == null) {
            _uiState.value = LyricsUiState.Idle
            return
        }
        _uiState.value = LyricsUiState.Loading
        val lines = lyricsRepository.load(song)
        _uiState.value = if (lines.isNullOrEmpty()) LyricsUiState.NoLyrics else LyricsUiState.Lyrics(lines)
    }
}
