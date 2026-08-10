package com.example.musicsiren.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitFactory {
    private const val BASE_URL = "https://monster-siren.hypergryph.com/"
    /** 自建云端 API 地址（公开，非机密）。与站点既有应用共用 /api/（后端走 index.php 路由）。 */
    const val CLOUD_BASE_URL = "https://sevencentury.cn/"

    fun createJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun createOkHttpClient(
        logLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BASIC,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = logLevel }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /** 云端 OkHttp：带 Bearer 鉴权拦截器（token 从内存态 AuthTokenProvider 同步读，不可在拦截器挂起）。
     *  日志保持 BASIC，避免 Authorization 头泄露进 logcat。 */
    fun createCloudOkHttpClient(tokenProvider: AuthTokenProvider): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = tokenProvider.token
                val request = if (token != null) {
                    chain.request().newBuilder().header("Authorization", "Bearer $token").build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun createApi(json: Json, client: OkHttpClient): SirenApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SirenApi::class.java)
    }

    fun createCloudApi(json: Json, client: OkHttpClient): CloudApi {
        return Retrofit.Builder()
            .baseUrl(CLOUD_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CloudApi::class.java)
    }
}
