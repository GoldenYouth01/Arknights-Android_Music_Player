package com.example.musicsiren.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicsiren.data.repository.AuthRepository
import com.example.musicsiren.data.repository.CloudApiException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _countdown = MutableStateFlow(0)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    fun sendCode(email: String) {
        if (_sending.value || _countdown.value > 0) return
        viewModelScope.launch {
            _sending.value = true
            _error.value = null
            try {
                authRepository.sendRegisterCode(email)
                startCountdown()
            } catch (e: CloudApiException) {
                _error.value = e.message
            } catch (e: IOException) {
                _error.value = "网络请求失败，请检查网络"
            } catch (e: Exception) {
                _error.value = "发送失败，请稍后再试"
            } finally {
                _sending.value = false
            }
        }
    }

    fun register(email: String, passwordHash: String, code: String, nickname: String?, onSuccess: () -> Unit) {
        if (_submitting.value) return
        viewModelScope.launch {
            _submitting.value = true
            _error.value = null
            try {
                authRepository.register(email, passwordHash, code, nickname)
                onSuccess()
            } catch (e: CloudApiException) {
                _error.value = e.message
            } catch (e: IOException) {
                _error.value = "网络请求失败，请检查网络"
            } catch (e: Exception) {
                _error.value = "注册失败，请稍后再试"
            } finally {
                _submitting.value = false
            }
        }
    }

    private fun startCountdown() {
        _countdown.value = 60
        viewModelScope.launch {
            while (_countdown.value > 0) {
                delay(1000)
                _countdown.value--
            }
        }
    }
}
