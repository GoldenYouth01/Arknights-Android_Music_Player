package com.example.musicsiren.ui.screens

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicsiren.data.repository.AuthRepository
import com.example.musicsiren.data.repository.CloudApiException
import com.example.musicsiren.domain.model.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException

/** 账号信息页：改昵称 / 传头像。 */
class AccountInfoViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val session: StateFlow<AuthSession?> = authRepository.session

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun saveNickname(nickname: String) = runCloud("昵称已更新") {
        authRepository.updateNickname(nickname)
    }

    fun uploadAvatar(bitmap: Bitmap) = runCloud("头像已更新") {
        val bytes = ByteArrayOutputStream().apply {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
        }.toByteArray()
        authRepository.uploadAvatar(bytes)
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun runCloud(successMsg: String, block: suspend () -> Unit) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            _message.value = null
            try {
                block()
                _message.value = successMsg
            } catch (e: CloudApiException) {
                _message.value = e.message
            } catch (e: IOException) {
                _message.value = "网络请求失败，请检查网络"
            } catch (e: Exception) {
                _message.value = "操作失败，请稍后再试"
            } finally {
                _busy.value = false
            }
        }
    }
}
