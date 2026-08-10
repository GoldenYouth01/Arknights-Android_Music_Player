package com.example.musicsiren.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicsiren.data.repository.CloudApiException
import com.example.musicsiren.data.repository.CloudRepository
import com.example.musicsiren.data.repository.PlaylistRepository
import com.example.musicsiren.domain.model.Playlist
import com.example.musicsiren.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class PlaylistDetailViewModel(
    private val playlistRepository: PlaylistRepository,
    private val cloudRepository: CloudRepository,
    private val playlistId: String,
) : ViewModel() {

    val playlists: StateFlow<List<Playlist>> = playlistRepository.playlists

    val playlist: Playlist?
        get() = playlistRepository.playlist(playlistId)

    val isLoggedIn: Boolean get() = cloudRepository.isLoggedIn

    private val _shareCode = MutableStateFlow<String?>(null)
    val shareCode: StateFlow<String?> = _shareCode.asStateFlow()

    private val _shareError = MutableStateFlow<String?>(null)
    val shareError: StateFlow<String?> = _shareError.asStateFlow()

    private val _sharing = MutableStateFlow(false)
    val sharing: StateFlow<Boolean> = _sharing.asStateFlow()

    fun removeSong(cid: String) = playlistRepository.removeSong(playlistId, cid)
    fun rename(name: String) = playlistRepository.renamePlaylist(playlistId, name)
    fun delete() = playlistRepository.deletePlaylist(playlistId)

    /** 生成/获取分享码。 */
    fun share() {
        if (_sharing.value) return
        viewModelScope.launch {
            _sharing.value = true
            _shareError.value = null
            try {
                _shareCode.value = cloudRepository.sharePlaylist(playlistId)
            } catch (e: CloudApiException) {
                _shareError.value = e.message
            } catch (e: IOException) {
                _shareError.value = "网络请求失败，请检查网络"
            } catch (e: Exception) {
                _shareError.value = "分享失败，请稍后再试"
            } finally {
                _sharing.value = false
            }
        }
    }

    fun clearShare() {
        _shareCode.value = null
        _shareError.value = null
    }

    /** 歌单歌曲 → 播放用领域 Song（sourceUrl 为空，由播放层 enrich 补齐）。 */
    fun toSongs(): List<Song> =
        playlist?.songs?.map {
            Song(cid = it.cid, name = it.name, albumCid = it.albumCid, artists = it.artists, sourceUrl = null, lyricUrl = null)
        } ?: emptyList()
}
