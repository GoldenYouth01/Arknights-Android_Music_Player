package com.example.musicsiren

import com.example.musicsiren.data.remote.ApiResponse
import com.example.musicsiren.data.remote.CloudPlaylistDto
import com.example.musicsiren.data.remote.EmptyData
import com.example.musicsiren.data.remote.HistoryListDto
import com.example.musicsiren.data.remote.LoginDataDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** 云端 API 响应契约解析（与 PHP 后端约定一致，code=0 成功）。 */
class CloudDtoParseTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `login response parses token and user`() {
        val raw = """
            {"code":0,"msg":"ok","data":{"token":"aaa.bbb.ccc","user":{"id":7,"email":"test@example.com","nickname":"博士"}}}
        """.trimIndent()
        val resp = json.decodeFromString(ApiResponse.serializer(LoginDataDto.serializer()), raw)
        assertEquals(0, resp.code)
        val data = requireNotNull(resp.data)
        assertEquals("aaa.bbb.ccc", data.token)
        assertEquals(7, data.user.id)
        assertEquals("test@example.com", data.user.email)
        assertEquals("博士", data.user.nickname)
    }

    @Test
    fun `empty data response parses to EmptyData`() {
        val raw = """{"code":0,"msg":"ok","data":{}}"""
        val resp = json.decodeFromString(ApiResponse.serializer(EmptyData.serializer()), raw)
        assertEquals(0, resp.code)
        assertEquals(EmptyData, resp.data)
    }

    @Test
    fun `business error parses with code and message`() {
        val raw = """{"code":1004,"msg":"邮箱或密码错误","data":{}}"""
        val resp = json.decodeFromString(ApiResponse.serializer(EmptyData.serializer()), raw)
        assertEquals(1004, resp.code)
        assertEquals("邮箱或密码错误", resp.msg)
    }

    @Test
    fun `playlist response parses songs with artists`() {
        val raw = """
            {"code":0,"msg":"ok","data":{"id":"1f6a4d3e-9c2b-4a5f-8b7e-000000000001","name":"云测试",
              "createdAt":1786780800000,"coverUrl":null,"shareCode":"ABCD2345",
              "songs":[{"cid":"1091","name":"塞壬唱片","artists":["塞壬唱片-MSR"],"albumCid":"8928","coverUrl":"https://x/y.png"}]}}
        """.trimIndent()
        val resp = json.decodeFromString(ApiResponse.serializer(CloudPlaylistDto.serializer()), raw)
        assertEquals(0, resp.code)
        val pl = requireNotNull(resp.data)
        assertEquals("ABCD2345", pl.shareCode)
        assertEquals(1, pl.songs.size)
        assertEquals(listOf("塞壬唱片-MSR"), pl.songs[0].artists)
    }

    @Test
    fun `history response parses list`() {
        val raw = """
            {"code":0,"msg":"ok","data":{"list":[
              {"cid":"1091","songName":"塞壬唱片","artists":["塞壬唱片-MSR"],"albumCid":"8928","coverUrl":null,"playedAt":1786780800000}
            ]}}
        """.trimIndent()
        val resp = json.decodeFromString(ApiResponse.serializer(HistoryListDto.serializer()), raw)
        val list = requireNotNull(resp.data).list
        assertEquals(1, list.size)
        assertEquals("塞壬唱片", list[0].songName)
        assertEquals(1786780800000L, list[0].playedAt)
        assertNull(list[0].coverUrl)
    }

    @Test
    fun `missing optional fields default gracefully`() {
        val raw = """{"code":0,"msg":"ok","data":{"id":"x","name":"空"}}"""
        val pl = json.decodeFromString(ApiResponse.serializer(CloudPlaylistDto.serializer()), raw).data
        requireNotNull(pl)
        assertTrue(pl.songs.isEmpty())
        assertNull(pl.shareCode)
        assertEquals(0L, pl.createdAt)
    }
}
