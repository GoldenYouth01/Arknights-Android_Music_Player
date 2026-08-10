package com.example.musicsiren.data.repository

import android.content.Context
import com.example.musicsiren.data.local.AuthStore
import com.example.musicsiren.data.remote.AuthTokenProvider
import com.example.musicsiren.data.remote.CloudApi
import com.example.musicsiren.data.remote.LoginReq
import com.example.musicsiren.data.remote.RegisterReq
import com.example.musicsiren.data.remote.ResetPasswordReq
import com.example.musicsiren.data.remote.SendCodeReq
import com.example.musicsiren.domain.model.AuthSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 账号会话的单一事实源：StateFlow<AuthSession?> + AuthStore 持久化 + AuthTokenProvider 供拦截器同步读。
 * init 用 first() 一次性加载（勿 collect，避免 DataStore 旧值覆盖，见踩坑 #5）。
 */
class AuthRepository(
    private val appContext: Context,
    private val store: AuthStore,
    private val api: CloudApi,
    private val tokenProvider: AuthTokenProvider,
    private val scope: CoroutineScope,
) {
    private val _session = MutableStateFlow<AuthSession?>(null)
    val session: StateFlow<AuthSession?> = _session.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            _session.value = store.session.first()
            tokenProvider.token = _session.value?.token
        }
    }

    suspend fun sendRegisterCode(email: String) {
        api.sendCode(SendCodeReq(email.trim().lowercase(), "register")).cloudDataOrThrowEmpty()
    }

    suspend fun sendResetCode(email: String) {
        api.sendCode(SendCodeReq(email.trim().lowercase(), "reset")).cloudDataOrThrowEmpty()
    }

    suspend fun register(email: String, passwordHash: String, code: String, nickname: String? = null) {
        val dto = api.register(RegisterReq(email.trim().lowercase(), passwordHash, code, nickname)).cloudDataOrThrow()
        setSession(dto.token, dto.user.id, dto.user.email, dto.user.nickname)
    }

    suspend fun login(email: String, passwordHash: String) {
        val dto = api.login(LoginReq(email.trim().lowercase(), passwordHash)).cloudDataOrThrow()
        setSession(dto.token, dto.user.id, dto.user.email, dto.user.nickname)
    }

    suspend fun resetPassword(email: String, code: String, passwordHash: String) {
        api.resetPassword(ResetPasswordReq(email.trim().lowercase(), code, passwordHash)).cloudDataOrThrowEmpty()
    }

    /** token 仍有效时刷新用户信息（昵称等）。 */
    suspend fun refreshMe() {
        val current = _session.value ?: return
        val user = api.me().cloudDataOrThrow()
        setSession(current.token, user.id, user.email, user.nickname)
    }

    fun logout() {
        _session.value = null
        tokenProvider.token = null
        scope.launch(Dispatchers.IO) { store.clear() }
    }

    private fun setSession(token: String, userId: Int, email: String, nickname: String?) {
        val session = AuthSession(token, userId, email, nickname)
        _session.value = session
        tokenProvider.token = token
        scope.launch(Dispatchers.IO) { store.save(session) }
    }
}
