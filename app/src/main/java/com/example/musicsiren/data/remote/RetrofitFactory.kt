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

    fun createApi(json: Json, client: OkHttpClient): SirenApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SirenApi::class.java)
    }
}
