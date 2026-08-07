package com.example.musicsiren.data.search

import com.example.musicsiren.data.repository.SirenRepository
import com.example.musicsiren.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface CatalogState {
    data object Idle : CatalogState
    data object Loading : CatalogState
    data class Ready(val songCount: Int) : CatalogState
    data class Error(val message: String?) : CatalogState
}

/**
 * 全量歌曲目录的内存索引（官网也是加载 /api/songs 后本地过滤）。
 * 支持随时 [refresh] 获取最新目录 —— 新上架的歌曲刷新后即可被搜索命中。
 */
class SongCatalog(
    private val songProvider: suspend () -> List<Song>,
) {
    constructor(repository: SirenRepository) : this({ repository.getSongsCatalog() })

    private val mutex = Mutex()
    @Volatile private var songs: List<Song> = emptyList()
    private val _state = MutableStateFlow<CatalogState>(CatalogState.Idle)
    val state: StateFlow<CatalogState> = _state.asStateFlow()

    suspend fun refresh(): Result<Int> = mutex.withLock {
        _state.value = CatalogState.Loading
        val result = runCatching { songProvider() }
        result.fold(
            onSuccess = { list ->
                songs = list
                _state.value = CatalogState.Ready(list.size)
                Result.success(list.size)
            },
            onFailure = { e ->
                _state.value = CatalogState.Error(e.message)
                Result.failure(e)
            },
        )
    }

    suspend fun ensureLoaded() {
        if (songs.isEmpty()) refresh()
    }

    suspend fun search(query: String): List<Song> {
        if (songs.isEmpty()) refresh()
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return songs.filter { song ->
            song.name.contains(q, ignoreCase = true) ||
                song.artists.any { it.contains(q, ignoreCase = true) }
        }
    }
}
