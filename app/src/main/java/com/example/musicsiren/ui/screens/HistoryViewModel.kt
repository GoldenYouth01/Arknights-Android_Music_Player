package com.example.musicsiren.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicsiren.data.repository.CloudRepository
import com.example.musicsiren.data.repository.HistoryRepository
import com.example.musicsiren.data.repository.SirenRepository
import com.example.musicsiren.domain.model.HistoryEntry
import com.example.musicsiren.playback.PlaybackViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** 播放历史：本地列表 + 已登录时拉取云端合并；点行播放（经 enrich 补齐 sourceUrl）。 */
class HistoryViewModel(
    private val historyRepository: HistoryRepository,
    private val cloudRepository: CloudRepository,
    private val sirenRepository: SirenRepository,
    private val playbackViewModel: PlaybackViewModel,
) : ViewModel() {

    val history: StateFlow<List<HistoryEntry>> = historyRepository.history

    init {
        // 已登录则拉取云端历史并合并（失败静默，本地仍可用）
        viewModelScope.launch {
            runCatching { cloudRepository.fetchHistory() }
        }
    }

    fun play(entry: HistoryEntry) {
        viewModelScope.launch {
            val song = runCatching { sirenRepository.getSong(entry.cid) }.getOrNull() ?: return@launch
            playbackViewModel.playSong(song, entry.coverUrl)
        }
    }
}
