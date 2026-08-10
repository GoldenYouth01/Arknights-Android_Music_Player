package com.example.musicsiren.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicsiren.di.AppContainer
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.util.PasswordHasher

/** 登录页：邮箱 + 密码；底部链接进入注册 / 找回密码。 */
@Composable
fun LoginScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onNavigateRegister: () -> Unit,
    onNavigateForgot: () -> Unit,
    viewModel: LoginViewModel = viewModel { LoginViewModel(container.authRepository) },
) {
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthScaffold(title = "登录", subtitle = "SIGN IN", onBack = onBack) {
        AuthTextField(email, { email = it }, label = "邮箱")
        Spacer(Modifier.height(12.dp))
        AuthTextField(password, { password = it }, label = "密码", isPassword = true)
        Spacer(Modifier.height(16.dp))

        error?.let {
            Text(it, style = SirenType.Body, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.login(email, PasswordHasher.hash(password), onSuccess = onBack) },
            enabled = email.isNotBlank() && password.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp, color = AccentCyan)
            } else {
                Text("登录")
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onNavigateRegister) { Text("注册新账号", color = AccentCyan) }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onNavigateForgot) { Text("忘记密码", color = AccentCyan) }
        }
    }
}
