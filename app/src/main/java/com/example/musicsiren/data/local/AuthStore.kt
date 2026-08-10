package com.example.musicsiren.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.musicsiren.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.authDataStore by preferencesDataStore(name = "auth")

/** 登录会话持久化（DataStore Preferences，整对象 JSON 单 key）。 */
class AuthStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    val session: Flow<AuthSession?> =
        context.authDataStore.data.map { prefs ->
            prefs[KEY]?.let { raw ->
                runCatching { json.decodeFromString(AuthSession.serializer(), raw) }.getOrNull()
            }
        }

    suspend fun save(session: AuthSession) {
        context.authDataStore.edit { prefs ->
            prefs[KEY] = json.encodeToString(AuthSession.serializer(), session)
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { prefs -> prefs.remove(KEY) }
    }

    private companion object {
        val KEY = stringPreferencesKey("session")
    }
}
