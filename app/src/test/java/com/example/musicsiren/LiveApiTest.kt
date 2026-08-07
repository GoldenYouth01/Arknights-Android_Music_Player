package com.example.musicsiren

import com.example.musicsiren.data.remote.RetrofitFactory
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 直连真实 API 验证 DTO 与线上一致（需要网络）。
 * 用 -Dlive.api=false 或注释本类可跳过（CI 无网络时）。
 */
class LiveApiTest {

    private val api = RetrofitFactory.createApi(
        json = RetrofitFactory.createJson(),
        client = RetrofitFactory.createOkHttpClient(HttpLoggingInterceptor.Level.NONE),
    )

    @Test
    fun `albums endpoint is live`() = runBlocking {
        val resp = api.albums()
        assertEquals(0, resp.code)
        assertTrue(!resp.data.isNullOrEmpty())
        val first = resp.data!!.first()
        assertNotNull(first.cid)
        assertNotNull(first.name)
    }

    @Test
    fun `album detail is live`() = runBlocking {
        val albums = api.albums().data!!
        val cid = albums.first().cid
        val resp = api.albumDetail(cid)
        assertEquals(0, resp.code)
        assertNotNull(resp.data)
        // 至少能解析（歌曲列表可能为空专辑）
        assertEquals(cid, resp.data!!.cid)
    }

    @Test
    fun `songs catalog is live`() = runBlocking {
        val resp = api.songs()
        assertEquals(0, resp.code)
        assertTrue(resp.data!!.list.isNotEmpty())
    }
}
