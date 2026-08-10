package com.example.musicsiren.di

import android.content.Context
import com.example.musicsiren.data.local.AuthStore
import com.example.musicsiren.data.local.DownloadStore
import com.example.musicsiren.data.local.HistoryStore
import com.example.musicsiren.data.local.PlaylistStore
import com.example.musicsiren.data.remote.AuthTokenProvider
import com.example.musicsiren.data.remote.CloudApi
import com.example.musicsiren.data.remote.RetrofitFactory
import com.example.musicsiren.data.remote.SirenApi
import com.example.musicsiren.data.repository.AuthRepository
import com.example.musicsiren.data.repository.CloudRepository
import com.example.musicsiren.data.repository.DownloadRepository
import com.example.musicsiren.data.repository.HistoryRepository
import com.example.musicsiren.data.repository.LyricsRepository
import com.example.musicsiren.data.repository.PlaylistRepository
import com.example.musicsiren.data.repository.SirenRepository
import com.example.musicsiren.data.search.SongCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient

/** 手动 DI 容器（学习项目刻意不用 Hilt：单例少、依赖显式可查）。 */
class AppContainer(context: Context) {

    val json = RetrofitFactory.createJson()
    val okHttpClient: OkHttpClient = RetrofitFactory.createOkHttpClient()
    val api: SirenApi = RetrofitFactory.createApi(json, okHttpClient)

    val sirenRepository = SirenRepository(api)
    val songCatalog = SongCatalog(sirenRepository)
    val lyricsRepository = LyricsRepository(okHttpClient)

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // —— 账号 / 云端（Siren 的 okHttpClient 保持原样，不带鉴权拦截器，避免 token 发给第三方）——
    val authTokenProvider = AuthTokenProvider()
    val cloudOkHttpClient: OkHttpClient = RetrofitFactory.createCloudOkHttpClient(authTokenProvider)
    val cloudApi: CloudApi = RetrofitFactory.createCloudApi(json, cloudOkHttpClient)

    val authStore = AuthStore(context.applicationContext)
    val authRepository = AuthRepository(
        context.applicationContext,
        authStore,
        cloudApi,
        authTokenProvider,
        appScope,
    )

    val historyStore = HistoryStore(context.applicationContext)
    val historyRepository = HistoryRepository(
        context.applicationContext,
        historyStore,
        appScope,
    )

    val downloadStore = DownloadStore(context.applicationContext)
    val downloadRepository = DownloadRepository(
        context.applicationContext,
        downloadStore,
        appScope,
    )
    val playlistStore = PlaylistStore(context.applicationContext)
    val playlistRepository = PlaylistRepository(
        context.applicationContext,
        playlistStore,
        appScope,
    )

    val cloudRepository = CloudRepository(
        context.applicationContext,
        cloudApi,
        authRepository,
        playlistRepository,
        historyRepository,
        appScope,
    )
}
