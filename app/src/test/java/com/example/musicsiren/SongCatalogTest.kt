package com.example.musicsiren

import com.example.musicsiren.data.search.SongCatalog
import com.example.musicsiren.domain.model.Song
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SongCatalogTest {

    private val fakeSongs = listOf(
        Song("1", "毛茸茸大决战！", "a", listOf("塞壬唱片-MSR"), null, null),
        Song("2", "Every Road is a Yes", "b", listOf("AIYUE blessed : 理名"), null, null),
        Song("3", "用不上的雨刷", "c", listOf("塞壬唱片-MSR", "塞壬唱片"), null, null),
    )

    private fun catalog() = SongCatalog { fakeSongs }

    @Test
    fun `search matches chinese song name`() = runBlocking {
        val catalog = catalog()
        catalog.refresh()
        assertEquals(1, catalog.search("毛茸茸").size)
    }

    @Test
    fun `search is case insensitive for latin`() = runBlocking {
        val catalog = catalog()
        catalog.refresh()
        assertTrue(catalog.search("every").isNotEmpty())
        assertTrue(catalog.search("EVERY").isNotEmpty())
    }

    @Test
    fun `search matches artist name`() = runBlocking {
        val catalog = catalog()
        catalog.refresh()
        assertEquals(2, catalog.search("塞壬").size)
    }

    @Test
    fun `blank query returns empty`() = runBlocking {
        val catalog = catalog()
        catalog.refresh()
        assertTrue(catalog.search("   ").isEmpty())
    }

    @Test
    fun `no match returns empty`() = runBlocking {
        val catalog = catalog()
        catalog.refresh()
        assertTrue(catalog.search("不存在的歌").isEmpty())
    }
}
