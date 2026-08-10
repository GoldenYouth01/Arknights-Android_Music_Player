package com.example.musicsiren

import com.example.musicsiren.data.lyrics.LyricParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** LRC 解析器测试（用 880385 真实 LRC 片段 + 边界情况）。 */
class LyricParserTest {

    @Test
    fun `parses standard lrc with millisecond timestamps`() {
        val lrc = """
            [00:26.129]Soaring through the city again today
            [00:29.021]I get to see a smile again
            [00:31.894]Nothing in the world could take its place
        """.trimIndent()
        val lines = LyricParser.parseLrc(lrc)
        assertEquals(3, lines.size)
        assertEquals(26_129L, lines[0].timeMs)
        assertEquals("Soaring through the city again today", lines[0].text)
        assertEquals(29_021L, lines[1].timeMs)
    }

    @Test
    fun `skips blank lines and meta tags`() {
        val lrc = "[ti:Test]\n[ar:Artist]\n\n\n[00:01.000]first\n\n[00:02.000]second\n"
        val lines = LyricParser.parseLrc(lrc)
        assertEquals(2, lines.size)
        assertEquals("first", lines[0].text)
        assertEquals("second", lines[1].text)
    }

    @Test
    fun `supports one line with multiple timestamps`() {
        val lrc = "[00:01.000][00:03.500]repeat me"
        val lines = LyricParser.parseLrc(lrc)
        assertEquals(2, lines.size)
        assertEquals(1_000L, lines[0].timeMs)
        assertEquals(3_500L, lines[1].timeMs)
        assertEquals("repeat me", lines[0].text)
    }

    @Test
    fun `sorts out-of-order lines ascending`() {
        val lrc = "[00:30.000]later\n[00:10.000]earlier\n[00:20.000]middle"
        val lines = LyricParser.parseLrc(lrc)
        assertEquals(listOf(10_000L, 20_000L, 30_000L), lines.map { it.timeMs })
        assertEquals("earlier", lines[0].text)
    }

    @Test
    fun `supports seconds without millis and 2-digit millis`() {
        val lrc = "[01:02]whole second\n[00:05.50]fifty millis"
        val lines = LyricParser.parseLrc(lrc)
        // 按时间升序排序：00:05.50(5500) 在前，01:02(62000) 在后
        assertEquals(5_500L, lines[0].timeMs)
        assertEquals("fifty millis", lines[0].text)
        assertEquals(62_000L, lines[1].timeMs)
        assertEquals("whole second", lines[1].text)
    }

    @Test
    fun `ignores lines without timestamps`() {
        val lrc = "[00:01.000]valid\njust some plain text\n"
        val lines = LyricParser.parseLrc(lrc)
        assertEquals(1, lines.size)
        assertEquals("valid", lines[0].text)
    }

    @Test
    fun `real sample from 880385`() {
        val lrc = """
            [00:26.129]Soaring through the city again today
            [00:29.021]I get to see a smile again
            [00:31.894]Nothing in the world could take its place
            [00:36.925]Cutting through the gentle breeze
        """.trimIndent()
        val lines = LyricParser.parseLrc(lrc)
        assertTrue(lines.isNotEmpty())
        assertEquals("Soaring through the city again today", lines.first().text)
        assertEquals(36_925L, lines.last().timeMs)
    }
}
