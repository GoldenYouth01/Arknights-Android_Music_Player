package com.example.musicsiren

import com.example.musicsiren.data.remote.ApiResponse
import com.example.musicsiren.data.remote.AlbumDto
import com.example.musicsiren.data.remote.SearchDataDto
import com.example.musicsiren.data.remote.SongDto
import com.example.musicsiren.data.remote.SongsDataDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 用真实接口返回的 JSON 结构（含 artistes/artists 字段坑）验证 DTO 解析契约。
 */
class DtoParseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun `albums response parses artistes`() {
        val raw = """{"code":0,"msg":"","data":[
            {"cid":"8928","name":"直到大地变成一颗酸橙OST","coverUrl":"https://web.hycdn.cn/siren/pic/x.png","artistes":["塞壬唱片-MSR"]},
            {"cid":"9377","name":"Heavenly Me","coverUrl":"https://web.hycdn.cn/siren/pic/y.jpg","artistes":["塞壬唱片-MSR","AIYUE blessed : 理名"]}
        ]}"""
        val resp = json.decodeFromString<ApiResponse<List<AlbumDto>>>(raw)
        assertEquals(0, resp.code)
        val albums = resp.data!!
        assertEquals(2, albums.size)
        assertEquals("8928", albums[0].cid)
        assertEquals(listOf("塞壬唱片-MSR"), albums[0].artistes)
    }

    @Test
    fun `song detail uses artists key`() {
        val raw = """{"code":0,"msg":"","data":{"cid":"232219","name":"用不上的雨刷","albumCid":"8928",
            "sourceUrl":"https://res01.hycdn.cn/x/siren/audio/a.wav","lyricUrl":null,"mvUrl":null,"mvCoverUrl":null,
            "artists":["塞壬唱片-MSR"]}}"""
        val song = json.decodeFromString<ApiResponse<SongDto>>(raw).data!!
        assertEquals("232219", song.cid)
        assertEquals(listOf("塞壬唱片-MSR"), song.artistNames())
        assertEquals("https://res01.hycdn.cn/x/siren/audio/a.wav", song.sourceUrl)
    }

    @Test
    fun `album detail songs fall back to artistes`() {
        val raw = """{"code":0,"msg":"","data":{"cid":"8928","name":"A","songs":[
            {"cid":"232219","name":"S1","artistes":["X"]},
            {"cid":"697674","name":"S2","artistes":[]}
        ]}}"""
        val resp = json.decodeFromString<ApiResponse<com.example.musicsiren.data.remote.AlbumDetailDto>>(raw)
        val songs = resp.data!!.songs
        assertEquals(listOf("X"), songs[0].artistNames())
        assertEquals(emptyList<String>(), songs[1].artistNames())
    }

    @Test
    fun `unknown keys are tolerated`() {
        val raw = """{"code":0,"msg":"","data":{"cid":"1","name":"A","extraUnknownKey":123}}"""
        val album = json.decodeFromString<ApiResponse<AlbumDto>>(raw).data!!
        assertEquals("A", album.name)
    }

    @Test
    fun `songs catalog parses`() {
        val raw = """{"code":0,"msg":"","data":{"list":[
            {"cid":"779442","name":"毛茸茸大决战！","albumCid":"8928","artists":["塞壬唱片-MSR"]}
        ],"autoplay":"048794"}}"""
        val data = json.decodeFromString<ApiResponse<SongsDataDto>>(raw).data!!
        assertEquals(1, data.list.size)
        assertEquals("048794", data.autoplay)
    }

    @Test
    fun `search response parses albums`() {
        val raw = """{"code":0,"msg":"","data":{"albums":{"list":[
            {"cid":"3883","name":"Every Road is a Yes","belong":"arknights","coverUrl":"https://x/c.png","artistes":["塞壬唱片-MSR"]}
        ],"end":true},"news":{"list":[],"end":true}}}"""
        val data = json.decodeFromString<ApiResponse<SearchDataDto>>(raw).data!!
        assertTrue(data.albums.end)
        assertEquals("Every Road is a Yes", data.albums.list[0].name)
    }
}
