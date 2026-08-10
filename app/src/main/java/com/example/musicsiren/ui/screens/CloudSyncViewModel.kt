package com.example.musicsiren.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicsiren.data.repository.CloudApiException
import com.example.musicsiren.data.repository.CloudRepository
import com.example.musicsiren.data.repository.PlaylistRepository
import com.example.musicsiren.domain.model.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

/** 云端歌单页：本地歌单列表 + 全量上传/下载/导入分享码。 */
class CloudSyncViewModel(
    private val cloudRepository: CloudRepository,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    val playlists: StateFlow<List<Playlist>> = playlistRepository.playlists

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun upload() = runCloud("已上传到云端") { cloudRepository.uploadPlaylists() }

    fun download() = runCloud("已从云端恢复本地歌单") { cloudRepository.downloadPlaylists() }

    fun importShare(code: String) = runCloud("歌单已导入") { cloudRepository.importShare(code) }

    fun clearMessage() {
        _message.value = null
    }

    private fun runCloud(successMsg: String, block: suspend () -> Unit) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            _message.value = null
            try {
                block()
                _message.value = successMsg
            } catch (e: CloudApiException) {
                _message.value = e.message
            } catch (e: IOException) {
                _message.value = "网络请求失败，请检查网络"
            } catch (e: Exception) {
                _message.value = "操作失败，请稍后再试"
            } finally {
                _busy.value = false
            }
        }
    }
}
