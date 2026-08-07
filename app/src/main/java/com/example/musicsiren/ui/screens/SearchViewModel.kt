package com.example.musicsiren.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicsiren.data.repository.SirenRepository
import com.example.musicsiren.data.search.SongCatalog
import com.example.musicsiren.domain.model.Album
import com.example.musicsiren.domain.model.Song
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val loading: Boolean = false,
)

/**
 * 搜索：歌曲走本地 SongCatalog 索引（/api/songs，可随时刷新），
 * 专辑走 /api/search；输入 300ms 防抖。
 */
@OptIn(FlowPreview::class)
class SearchViewModel(
    private val songCatalog: SongCatalog,
    private val repository: SirenRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.value = SearchUiState()
                        return@collectLatest
                    }
                    _uiState.value = _uiState.value.copy(query = query, loading = true)
                    val songs = songCatalog.search(query)
                    val albums = runCatching { repository.searchAlbums(query) }.getOrDefault(emptyList())
                    _uiState.value = SearchUiState(query = query, songs = songs, albums = albums)
                }
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun refreshCatalog() {
        viewModelScope.launch { songCatalog.refresh() }
    }
}
