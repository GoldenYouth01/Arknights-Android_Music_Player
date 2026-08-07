package com.example.musicsiren.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.musicsiren.domain.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.playlistDataStore by preferencesDataStore(name = "playlists")

/** 歌单的持久化层（DataStore + 序列化 JSON，与 DownloadStore 同一套模式）。 */
class PlaylistStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val playlists: Flow<List<Playlist>> =
        context.playlistDataStore.data.map { prefs ->
            prefs[KEY]?.let { raw ->
                runCatching {
                    json.decodeFromString(ListSerializer(Playlist.serializer()), raw)
                }.getOrDefault(emptyList())
            } ?: emptyList()
        }

    suspend fun saveAll(playlists: List<Playlist>) {
        context.playlistDataStore.edit { prefs ->
            prefs[KEY] = json.encodeToString(ListSerializer(Playlist.serializer()), playlists)
        }
    }

    private companion object {
        val KEY = stringPreferencesKey("playlists")
    }
}
