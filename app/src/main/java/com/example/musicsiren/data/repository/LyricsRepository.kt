package com.example.musicsiren.data.repository

import com.example.musicsiren.data.lyrics.LyricParser
import com.example.musicsiren.domain.model.LyricLine
import com.example.musicsiren.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * 歌词加载：根据 Song.lyricUrl 抓取 .lrc 并解析。
 * 无 lyricUrl / 抓取失败 / 无有效行 → 返回 null（调用方显示「纯音乐」）。
 */
class LyricsRepository(private val client: OkHttpClient) {

    @Volatile private var cacheCid: String? = null
    @Volatile private var cacheLines: List<LyricLine>? = null

    suspend fun load(song: Song): List<LyricLine>? {
        if (song.cid == cacheCid) return cacheLines
        val url = song.lyricUrl
        val lines = if (url.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = client.newCall(Request.Builder().url(url).build()).execute()
                    val body = response.use { r ->
                        if (!r.isSuccessful) null else r.body?.string()?.trim()
                    }
                    body?.let { LyricParser.parseLrc(it) }?.takeIf { it.isNotEmpty() }
                }.getOrNull()
            }
        }
        cacheCid = song.cid
        cacheLines = lines
        return lines
    }
}
