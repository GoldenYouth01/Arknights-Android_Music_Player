package com.example.musicsiren.data.lyrics

import com.example.musicsiren.domain.model.LyricLine

/**
 * LRC 歌词解析器（纯函数，可单测）。
 * 支持 [mm:ss]、[mm:ss.mmm]、[mm:ss.xx]；一行多个时间戳；自动跳过空行与无时间戳行；按时间升序排序。
 */
object LyricParser {

    private val TIME_TAG = Regex("\\[(\\d{1,2}):(\\d{1,2}(?:\\.\\d{1,3})?)]")

    fun parseLrc(content: String): List<LyricLine> {
        val result = mutableListOf<LyricLine>()
        for (rawLine in content.lines()) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue
            val timestamps = TIME_TAG.findAll(line).map { m ->
                val minutes = m.groupValues[1].toLong()
                val seconds = m.groupValues[2].toDoubleOrNull() ?: 0.0
                minutes * 60_000L + (seconds * 1000.0).toLong()
            }.toList()
            if (timestamps.isEmpty()) continue // 元信息行 / 无时间戳行
            val text = line.replace(TIME_TAG, "").trim()
            if (text.isEmpty()) continue
            timestamps.forEach { t -> result += LyricLine(t, text) }
        }
        return result.sortedBy { it.timeMs }
    }
}
