package com.example.musicsiren.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicsiren.data.repository.DownloadRepository
import com.example.musicsiren.data.repository.SirenRepository
import com.example.musicsiren.domain.model.Album
import com.example.musicsiren.domain.model.AlbumDetail
import com.example.musicsiren.domain.model.DownloadRecord
import com.example.musicsiren.domain.model.DownloadStatus
import com.example.musicsiren.domain.model.Song
import com.example.musicsiren.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlbumDetailViewModel(
    private val repository: SirenRepository,
    private val downloadRepository: DownloadRepository,
    private val cid: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AlbumDetail>>(UiState.Loading)
    val uiState: StateFlow<UiState<AlbumDetail>> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = runCatching { repository.getAlbumDetail(cid) }
                .fold(
                    onSuccess = { UiState.Data(it) },
                    onFailure = { UiState.Error(it.message) },
                )
        }
    }

    /** 每次进入都重新拉取最新详情；下拉刷新失败保留旧数据。 */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _uiState.value = UiState.Data(repository.getAlbumDetail(cid))
            } catch (e: Exception) {
                if (_uiState.value !is UiState.Data<*>) {
                    _uiState.value = UiState.Error(e.message)
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * 已下载→删除本地文件；否则入队下载（ERROR 视为重新下载）。
     */
    fun toggleDownload(song: Song, album: Album) {
        val existing = downloadRepository.recordFor(song.cid)
        if (existing?.status == DownloadStatus.DOWNLOADED) {
            downloadRepository.cancelAndDelete(song.cid)
            return
        }
        downloadRepository.enqueueDownload(
            DownloadRecord(
                songCid = song.cid,
                songName = song.name,
                artistNames = song.artists,
                albumCid = album.cid,
                albumName = album.name,
                coverUrl = album.coverUrl,
                sourceUrl = song.sourceUrl ?: "",
            )
        )
    }
}
