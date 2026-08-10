package com.example.musicsiren.data.repository

import android.content.Context
import com.example.musicsiren.data.local.HistoryStore
import com.example.musicsiren.domain.model.HistoryEntry
import com.example.musicsiren.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** 播放历史的本地单一事实源（云端同步经 CloudRepository）。保留最近 200 条。 */
class HistoryRepository(
    private val appContext: Context,
    private val store: HistoryStore,
    private val scope: CoroutineScope,
) {
    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            _history.value = store.history.first()
        }
    }

    /** 记录一次播放：同 cid 更新元数据/时间并移到最前（最近播放去重）。 */
    fun addSong(song: Song, coverUrl: String? = null) {
        val entry = HistoryEntry(
            cid = song.cid,
            name = song.name,
            artists = song.artists,
            albumCid = song.albumCid,
            coverUrl = coverUrl,
            playedAt = System.currentTimeMillis(),
        )
        val updated = listOf(entry) + _history.value.filterNot { it.cid == entry.cid }
        persist(updated.take(200))
    }

    /** 用云端/合并后的列表替换本地。 */
    fun replaceAll(entries: List<HistoryEntry>) {
        persist(entries.take(200))
    }

    fun latestEntry(): HistoryEntry? = _history.value.firstOrNull()

    private fun persist(list: List<HistoryEntry>) {
        _history.value = list
        scope.launch(Dispatchers.IO) { store.saveAll(list) }
    }
}
