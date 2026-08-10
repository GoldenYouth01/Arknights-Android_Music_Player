package com.example.musicsiren.data.repository

import android.content.Context
import com.example.musicsiren.data.remote.CloudApi
import com.example.musicsiren.data.remote.CloudPlaylistDto
import com.example.musicsiren.data.remote.CloudSongDto
import com.example.musicsiren.data.remote.HistoryAddReq
import com.example.musicsiren.data.remote.SyncPlaylistsReq
import com.example.musicsiren.domain.model.HistoryEntry
import com.example.musicsiren.domain.model.Playlist
import com.example.musicsiren.domain.model.PlaylistSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 云端歌单/历史编排：连接 AuthRepository 与本地 Playlist/History 仓库。
 * 同步语义：全量上传覆盖服务器副本 / 全量下载覆盖本地（UI 需确认）。
 */
class CloudRepository(
    private val appContext: Context,
    private val api: CloudApi,
    private val authRepository: AuthRepository,
    private val playlistRepository: PlaylistRepository,
    private val historyRepository: HistoryRepository,
    private val scope: CoroutineScope,
) {
    val isLoggedIn: Boolean get() = authRepository.session.value != null

    /** 全量上传本地歌单到云端（覆盖服务器该用户副本）。 */
    suspend fun uploadPlaylists() {
        val incoming = playlistRepository.playlists.value.map { it.toCloud() }
        api.syncPlaylists(SyncPlaylistsReq(incoming)).cloudDataOrThrowEmpty()
        markAllSynced()
    }

    /** 全量下载云端歌单到本地（覆盖本地；调用前 UI 需确认）。 */
    suspend fun downloadPlaylists() {
        val dto = api.playlists().cloudDataOrThrow()
        playlistRepository.replaceAll(dto.playlists.map { it.toLocal() })
    }

    /** 生成/获取分享码。 */
    suspend fun sharePlaylist(id: String): String =
        api.share(id).cloudDataOrThrow().code

    suspend fun unsharePlaylist(id: String) {
        api.unshare(id).cloudDataOrThrowEmpty()
    }

    /** 按分享码导入：服务端在当前账号下建副本并去重，返回副本落到本地。 */
    suspend fun importShare(code: String) {
        val dto = api.saveShare(code.trim().uppercase()).cloudDataOrThrow()
        playlistRepository.importShared(dto.toLocal())
    }

    /** 上传单条播放历史（调用方已 runCatching 包住，不影响播放）。 */
    suspend fun uploadHistory(entry: HistoryEntry) {
        api.addHistory(
            HistoryAddReq(
                cid = entry.cid,
                songName = entry.name,
                artists = entry.artists,
                albumCid = entry.albumCid,
                coverUrl = entry.coverUrl,
                playedAt = entry.playedAt,
            )
        ).cloudDataOrThrowEmpty()
    }

    /** 拉取云端历史并与本地合并（同 cid 取 playedAt 较新者）。 */
    suspend fun fetchHistory() {
        if (authRepository.session.value == null) return
        val dto = api.history().cloudDataOrThrow()
        val cloud = dto.list.map {
            HistoryEntry(it.cid, it.songName, it.artists, it.albumCid, it.coverUrl, it.playedAt)
        }
        val merged = (cloud + historyRepository.history.value)
            .groupBy { it.cid }
            .map { (_, entries) -> entries.maxBy { it.playedAt } }
            .sortedByDescending { it.playedAt }
        historyRepository.replaceAll(merged)
    }

    private fun markAllSynced() {
        scope.launch(Dispatchers.IO) { playlistRepository.markAllSynced() }
    }
}

private fun Playlist.toCloud() = CloudPlaylistDto(
    id = id,
    name = name,
    createdAt = createdAt,
    coverUrl = coverUrl,
    songs = songs.map { it.toCloud() },
)

private fun PlaylistSong.toCloud() = CloudSongDto(cid, name, artists, albumCid, coverUrl)

private fun CloudPlaylistDto.toLocal() = Playlist(
    id = id,
    name = name,
    createdAt = createdAt,
    coverUrl = coverUrl,
    songs = songs.map { it.toLocal() },
    cloudId = id,
)

private fun CloudSongDto.toLocal() = PlaylistSong(cid, name, artists, albumCid, coverUrl)
