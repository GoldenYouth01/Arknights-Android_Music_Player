package com.example.musicsiren.domain.model

/** LRC 歌词的一行：时间戳（毫秒）+ 文本。 */
data class LyricLine(
    val timeMs: Long,
    val text: String,
)
