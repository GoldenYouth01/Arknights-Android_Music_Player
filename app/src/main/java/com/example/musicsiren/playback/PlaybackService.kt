package com.example.musicsiren.playback

import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.musicsiren.MusicApp

/**
 * 后台播放服务（Media3）。
 * 用 OkHttpDataSource 流式播放 WAV（支持 Range/seek）；自动提供媒体通知、锁屏与蓝牙控制。
 */
class PlaybackService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()
        val app = applicationContext as MusicApp
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(this)
                    // 关键：用 DefaultDataSource 包裹，file:// 本地文件走 FileDataSource，
                    // http/https 走 OkHttp；否则本地离线播放会 Source error。
                    .setDataSourceFactory(
                        DefaultDataSource.Factory(this, OkHttpDataSource.Factory(app.container.okHttpClient))
                    )
            )
            .build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    override fun onDestroy() {
        player.release()
        mediaSession.release()
        super.onDestroy()
    }
}
