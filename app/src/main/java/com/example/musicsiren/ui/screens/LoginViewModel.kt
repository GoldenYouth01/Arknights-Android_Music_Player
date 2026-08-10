package com.example.musicsiren.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicsiren.data.repository.AuthRepository
import com.example.musicsiren.data.repository.CloudApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun login(email: String, passwordHash: String, onSuccess: () -> Unit) {
        if (_loading.value) return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                authRepository.login(email, passwordHash)
                onSuccess()
            } catch (e: CloudApiException) {
                _error.value = e.message
            } catch (e: IOException) {
                _error.value = "网络请求失败，请检查网络"
            } catch (e: Exception) {
                _error.value = "登录失败，请稍后再试"
            } finally {
                _loading.value = false
            }
        }
    }
}
