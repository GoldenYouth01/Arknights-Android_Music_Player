package com.example.musicsiren.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicsiren.data.repository.SirenRepository
import com.example.musicsiren.domain.model.Album
import com.example.musicsiren.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlbumListViewModel(private val repository: SirenRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Album>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Album>>> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = runCatching { repository.getAlbums() }
                .fold(
                    onSuccess = { UiState.Data(it) },
                    onFailure = { UiState.Error(it.message) },
                )
        }
    }

    /** 下拉刷新：失败时保留旧数据，仅当当前无数据才显示错误态。 */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _uiState.value = UiState.Data(repository.getAlbums())
            } catch (e: Exception) {
                if (_uiState.value !is UiState.Data<*>) {
                    _uiState.value = UiState.Error(e.message)
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
