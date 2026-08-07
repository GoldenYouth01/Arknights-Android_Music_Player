package com.example.musicsiren.data.repository

import android.content.Context
import com.example.musicsiren.data.local.PlaylistStore
import com.example.musicsiren.domain.model.Playlist
import com.example.musicsiren.domain.model.PlaylistSong
import com.example.musicsiren.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 歌单的单一事实源：内存 StateFlow 提供实时更新，DataStore 持久化。
 * 歌曲以快照（PlaylistSong）存储，离线可显示；sourceUrl 播放时补齐。
 */
class PlaylistRepository(
    private val appContext: Context,
    private val store: PlaylistStore,
    private val scope: CoroutineScope,
) {
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    init {
        // 启动时一次性加载持久化数据。
        // 不用持续 collect：避免 DataStore 旧值发射覆盖后续内存修改（否则会出现"刚加的歌消失"）。
        scope.launch(Dispatchers.IO) {
            _playlists.value = store.playlists.first()
        }
    }

    fun playlist(id: String): Playlist? = _playlists.value.find { it.id == id }

    /** 新建歌单；[firstSong] 非空则同时加入并作为歌单封面来源。返回歌单 id（空名为空串）。 */
    fun createPlaylist(name: String, firstSong: Song? = null, coverUrl: String? = null): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return ""
        val id = UUID.randomUUID().toString()
        val song = firstSong?.let { PlaylistSong(it.cid, it.name, it.artists, it.albumCid, coverUrl) }
        val playlist = Playlist(
            id = id,
            name = trimmed,
            createdAt = System.currentTimeMillis(),
            coverUrl = if (song != null) coverUrl else null,
            songs = if (song != null) listOf(song) else emptyList(),
        )
        persist(_playlists.value + playlist)
        return id
    }

    fun addSong(playlistId: String, song: Song, coverUrl: String? = null) {
        val list = _playlists.value
        val idx = list.indexOfFirst { it.id == playlistId }
        if (idx < 0) return
        val current = list[idx]
        if (current.songs.any { it.cid == song.cid }) return // 去重
        val newSong = PlaylistSong(song.cid, song.name, song.artists, song.albumCid, coverUrl)
        val updated = current.copy(
            coverUrl = current.coverUrl ?: coverUrl,
            songs = current.songs + newSong,
        )
        persist(list.toMutableList().also { it[idx] = updated })
    }

    fun removeSong(playlistId: String, cid: String) {
        val list = _playlists.value
        val idx = list.indexOfFirst { it.id == playlistId }
        if (idx < 0) return
        val updated = list[idx].copy(songs = list[idx].songs.filterNot { it.cid == cid })
        persist(list.toMutableList().also { it[idx] = updated })
    }

    fun deletePlaylist(id: String) {
        persist(_playlists.value.filterNot { it.id == id })
    }

    fun renamePlaylist(id: String, name: String) {
        val list = _playlists.value
        val idx = list.indexOfFirst { it.id == id }
        if (idx < 0) return
        persist(list.toMutableList().also { it[idx] = it[idx].copy(name = name.trim()) })
    }

    private fun persist(list: List<Playlist>) {
        _playlists.value = list
        scope.launch(Dispatchers.IO) { store.saveAll(list) }
    }
}
