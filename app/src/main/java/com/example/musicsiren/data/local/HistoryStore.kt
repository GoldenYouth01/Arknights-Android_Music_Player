package com.example.musicsiren.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.musicsiren.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.historyDataStore by preferencesDataStore(name = "history")

/** 播放历史持久化（同 PlaylistStore 模式：整表 JSON 单 key）。 */
class HistoryStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    val history: Flow<List<HistoryEntry>> =
        context.historyDataStore.data.map { prefs ->
            prefs[KEY]?.let { raw ->
                runCatching {
                    json.decodeFromString(ListSerializer(HistoryEntry.serializer()), raw)
                }.getOrDefault(emptyList())
            } ?: emptyList()
        }

    suspend fun saveAll(entries: List<HistoryEntry>) {
        context.historyDataStore.edit { prefs ->
            prefs[KEY] = json.encodeToString(ListSerializer(HistoryEntry.serializer()), entries)
        }
    }

    private companion object {
        val KEY = stringPreferencesKey("history")
    }
}
