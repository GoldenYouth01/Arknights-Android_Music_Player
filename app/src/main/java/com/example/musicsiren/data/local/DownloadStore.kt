package com.example.musicsiren.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.musicsiren.domain.model.DownloadRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.downloadDataStore by preferencesDataStore(name = "downloads")

/** 下载记录的持久化层（DataStore + 序列化 JSON，替代 Room）。 */
class DownloadStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val records: Flow<List<DownloadRecord>> =
        context.downloadDataStore.data.map { prefs ->
            prefs[KEY]?.let { raw ->
                runCatching {
                    json.decodeFromString(ListSerializer(DownloadRecord.serializer()), raw)
                }.getOrDefault(emptyList())
            } ?: emptyList()
        }

    suspend fun saveAll(records: List<DownloadRecord>) {
        context.downloadDataStore.edit { prefs ->
            prefs[KEY] = json.encodeToString(ListSerializer(DownloadRecord.serializer()), records)
        }
    }

    private companion object {
        val KEY = stringPreferencesKey("records")
    }
}
