package com.example.musicsiren.playback

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicsiren.data.repository.CloudRepository
import com.example.musicsiren.data.repository.DownloadRepository
import com.example.musicsiren.data.repository.HistoryRepository
import com.example.musicsiren.data.repository.SirenRepository
import com.example.musicsiren.domain.model.Song
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class PlaybackUiState(
    val hasCurrent: Boolean = false,
    val isPlaying: Boolean = false,
    val currentSong: Song? = null,
    val queue: List<Song> = emptyList(),
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val coverUrl: String? = null,
    val albumName: String? = null,
    val isDownloaded: Boolean = false,
    // 播放模式：作用于当前队列（即播放歌曲时所在的列表）
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val shuffleEnabled: Boolean = false,
)

/**
 * 全局播放状态：绑定 Media3 MediaController，暴露 [PlaybackUiState]。
 * 由 Activity 作用域持有（全 App 单实例），供各屏幕与底部播放条共享。
 */
class PlaybackViewModel(
    application: Application,
    private val sirenRepository: SirenRepository,
    private val downloadRepository: DownloadRepository,
    private val historyRepository: HistoryRepository,
    private val cloudRepository: CloudRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private val _nowPlayingVisible = MutableStateFlow(false)
    val nowPlayingVisible: StateFlow<Boolean> = _nowPlayingVisible.asStateFlow()

    private val _drawerVisible = MutableStateFlow(false)
    val drawerVisible: StateFlow<Boolean> = _drawerVisible.asStateFlow()

    private var controller: Player? = null

    // 与 Media3 队列对应的领域队列（供抽屉渲染完整歌曲信息）
    @Volatile private var currentQueue: List<Song> = emptyList()
    @Volatile private var currentCoverUrl: String? = null
    @Volatile private var currentAlbumName: String? = null

    // 播放历史：当前歌曲 cid 变化时本地记录 + 防抖 3s 后上传云端（fire-and-forget）
    @Volatile private var lastHistoryCid: String? = null
    private var historyUploadJob: Job? = null

    init {
        bindController()
        // 位置 ticker：StateFlow 自动去重，仅在播放中产生重组
        viewModelScope.launch {
            while (isActive) {
                updateFromPlayer()
                delay(500)
            }
        }
    }

    /** 异步绑定 Media3 MediaController（用主线程 executor 等待 buildAsync 完成）。 */
    private fun bindController() {
        val app = getApplication<Application>()
        val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        future.addListener(
            {
                controller = future.get()
                controller?.addListener(playbackListener)
                updateFromPlayer()
            },
            ContextCompat.getMainExecutor(app),
        )
    }

    private val playbackListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) = updateFromPlayer()
        override fun onPlaybackStateChanged(playbackState: Int) = updateFromPlayer()
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = updateFromPlayer()
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) = updateFromPlayer()
        override fun onRepeatModeChanged(repeatMode: Int) = updateFromPlayer()
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) = updateFromPlayer()
    }

    private fun updateFromPlayer() {
        val player = controller ?: return
        val index = player.currentMediaItemIndex
        var song = currentQueue.getOrNull(index)
        var coverUrl = currentCoverUrl
        var albumName = currentAlbumName

        // 应用重启后内存队列丢失，但存活服务中的播放器仍持有 MediaItem：
        // 从 metadata 重建队列（封面/歌名立即可恢复），并异步补齐 sourceUrl/lyricUrl（歌词页需要）
        if (song == null && currentQueue.isEmpty() && player.mediaItemCount > 0) {
            val rebuilt = (0 until player.mediaItemCount).map { songFromMediaItem(player.getMediaItemAt(it)) }
            if (rebuilt.isNotEmpty()) {
                currentQueue = rebuilt
                val md = player.getMediaItemAt(index).mediaMetadata
                coverUrl = md.artworkUri?.toString()
                albumName = md.albumTitle?.toString()
                currentCoverUrl = coverUrl
                currentAlbumName = albumName
                song = currentQueue.getOrNull(index)
                viewModelScope.launch {
                    val target = currentQueue
                    val enriched = enrichWithUrls(target)
                    if (currentQueue === target) {   // 期间未被 playQueue 替换才应用
                        currentQueue = enriched
                        updateFromPlayer()
                    }
                }
            }
        }

        _uiState.value = PlaybackUiState(
            hasCurrent = player.mediaItemCount > 0,
            isPlaying = player.isPlaying,
            currentSong = song,
            queue = currentQueue,
            positionMs = player.currentPosition,
            durationMs = player.duration.coerceAtLeast(0L),
            coverUrl = coverUrl,
            albumName = albumName,
            isDownloaded = song?.let { downloadRepository.localPathFor(it.cid) != null } == true,
            repeatMode = player.repeatMode,
            shuffleEnabled = player.shuffleModeEnabled,
        )

        recordHistory(song, coverUrl)
    }

    /** 当前歌曲变化时：本地记录历史 + 防抖上传云端（失败不影响播放）。 */
    private fun recordHistory(song: Song?, coverUrl: String?) {
        val cid = song?.cid
        if (cid == null || cid == lastHistoryCid) return
        lastHistoryCid = cid
        historyRepository.addSong(song, coverUrl)
        historyUploadJob?.cancel()
        historyUploadJob = viewModelScope.launch {
            delay(3_000) // 连续切歌只上传最后一次
            if (cloudRepository.isLoggedIn) {
                historyRepository.latestEntry()?.let { entry ->
                    runCatching { cloudRepository.uploadHistory(entry) }
                }
            }
        }
    }

    fun playQueue(songs: List<Song>, startIndex: Int, coverUrl: String?, albumName: String?) {
        if (songs.isEmpty()) return
        val player = controller ?: return
        currentQueue = songs
        currentCoverUrl = coverUrl
        currentAlbumName = albumName
        viewModelScope.launch {
            val enriched = enrichWithUrls(songs)
            // 补全 sourceUrl/lyricUrl 后再作为 UI 队列（歌词页需要 lyricUrl）
            currentQueue = enriched
            val items = enriched.map { song -> buildMediaItem(song, coverUrl, albumName) }
            player.setMediaItems(items, startIndex.coerceIn(items.indices), 0L)
            player.prepare()
            player.play()
        }
    }

    /** 播放单曲（离线下载页用）。 */
    fun playSong(song: Song, coverUrl: String? = null, albumName: String? = null) {
        playQueue(listOf(song), 0, coverUrl, albumName)
    }

    private fun buildMediaItem(song: Song, coverUrl: String?, albumName: String?): MediaItem {
        val localPath = downloadRepository.localPathFor(song.cid)
        val uri = if (localPath != null) Uri.fromFile(File(localPath)) else Uri.parse(song.sourceUrl)
        return MediaItem.Builder()
            .setMediaId(song.cid)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.name)
                    .setArtist(song.artists.joinToString(" / "))
                    .setAlbumTitle(albumName)
                    .setArtworkUri(coverUrl?.let { Uri.parse(it) })
                    .build()
            )
            .build()
    }

    /** 从播放器 MediaItem 的 metadata 重建轻量 Song（重启恢复用；sourceUrl/lyricUrl 由 enrichWithUrls 补齐）。 */
    private fun songFromMediaItem(media: MediaItem): Song {
        val md = media.mediaMetadata
        return Song(
            cid = media.mediaId,
            name = md.title?.toString() ?: "",
            albumCid = null,
            artists = md.artist?.toString()?.split(" / ")?.filter { it.isNotBlank() } ?: emptyList(),
            sourceUrl = null,
            lyricUrl = null,
        )
    }

    /**
     * /api/songs 目录里的歌曲没有 sourceUrl，播放前异步补齐。
     * 已补齐的（来自专辑详情）直接复用，避免多余网络请求。
     */
    private suspend fun enrichWithUrls(songs: List<Song>): List<Song> {
        if (songs.all { !it.sourceUrl.isNullOrBlank() }) return songs
        return coroutineScope {
            val resolved = songs
                .filter { it.sourceUrl.isNullOrBlank() }
                .map { it.cid }
                .distinct()
                .map { cid -> async { cid to runCatching { sirenRepository.getSong(cid) }.getOrNull() } }
                .awaitAll()
                .mapNotNull { (cid, full) -> if (full != null) cid to full else null }
                .toMap()
            songs.map { resolved[it.cid] ?: it }
        }
    }

    fun togglePlay() = controller?.let { if (it.isPlaying) it.pause() else it.play() }
    fun next() = controller?.seekToNextMediaItem()
    fun previous() = controller?.seekToPreviousMediaItem()

    /** 循环模式：顺序 → 全部循环 → 单曲循环 → 顺序 */
    fun cycleRepeatMode() {
        val player = controller ?: return
        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun toggleShuffle() {
        val player = controller ?: return
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }
    fun seekTo(ms: Long) = controller?.seekTo(ms.coerceAtLeast(0L))
    fun seekToIndex(index: Int) = controller?.let {
        it.seekToDefaultPosition(index)
        it.play()
    }

    fun showNowPlaying() { _nowPlayingVisible.value = true }
    fun hideNowPlaying() { _nowPlayingVisible.value = false }
    fun showDrawer() { _drawerVisible.value = true }
    fun hideDrawer() { _drawerVisible.value = false }
}
