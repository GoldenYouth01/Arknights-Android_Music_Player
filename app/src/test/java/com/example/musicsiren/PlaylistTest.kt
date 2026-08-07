package com.example.musicsiren

import com.example.musicsiren.domain.model.Playlist
import com.example.musicsiren.domain.model.PlaylistSong
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

/** 歌单模型 JSON 序列化往返（DataStore 持久化的核心契约）。 */
class PlaylistTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `playlist round trip preserves chinese and latin names`() {
        val playlist = Playlist(
            id = "abc-123",
            name = "测试歌单",
            coverUrl = "https://web.hycdn.cn/siren/pic/x.png",
            songs = listOf(
                PlaylistSong("1", "毛茸茸大决战！", listOf("塞壬唱片-MSR"), "8928"),
                PlaylistSong("2", "Every Road is a Yes", listOf("AIYUE blessed"), "3883"),
            ),
        )
        val encoded = json.encodeToString(Playlist.serializer(), playlist)
        val decoded = json.decodeFromString(Playlist.serializer(), encoded)
        assertEquals(playlist, decoded)
    }

    @Test
    fun `empty playlist round trips`() {
        val pl = Playlist(id = "x", name = "空歌单")
        val round = json.decodeFromString(Playlist.serializer(), json.encodeToString(Playlist.serializer(), pl))
        assertEquals(pl, round)
    }

    @Test
    fun `list of playlists round trips`() {
        val list = listOf(
            Playlist(id = "1", name = "A", songs = listOf(PlaylistSong("a", "s1", listOf("X")))),
            Playlist(id = "2", name = "B"),
        )
        val encoded = json.encodeToString(ListSerializer(Playlist.serializer()), list)
        val decoded = json.decodeFromString(ListSerializer(Playlist.serializer()), encoded)
        assertEquals(list, decoded)
    }
}
