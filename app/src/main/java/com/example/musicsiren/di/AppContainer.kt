package com.example.musicsiren.di

import android.content.Context
import com.example.musicsiren.data.local.DownloadStore
import com.example.musicsiren.data.local.PlaylistStore
import com.example.musicsiren.data.remote.RetrofitFactory
import com.example.musicsiren.data.remote.SirenApi
import com.example.musicsiren.data.repository.DownloadRepository
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

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
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
}
