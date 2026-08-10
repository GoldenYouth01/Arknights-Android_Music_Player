package com.example.musicsiren.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicsiren.di.AppContainer
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.SurfaceDark
import com.example.musicsiren.ui.theme.TextSecondary
import com.example.musicsiren.util.PasswordHasher

/** 找回密码页：邮箱 + 验证码 + 新密码；成功后提示返回登录。 */
@Composable
fun ForgotPasswordScreen(
    container: AppContainer,
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel { ForgotPasswordViewModel(container.authRepository) },
) {
    val sending by viewModel.sending.collectAsStateWithLifecycle()
    val submitting by viewModel.submitting.collectAsStateWithLifecycle()
    val countdown by viewModel.countdown.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val success by viewModel.success.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val passwordValid = password.length >= 8

    AuthScaffold(title = "找回密码", subtitle = "RESET PASSWORD", onBack = onBack) {
        Text("验证码将发送到你的邮箱，10 分钟内有效。", style = SirenType.Body, color = TextSecondary)
        Spacer(Modifier.height(16.dp))
        AuthTextField(email, { email = it }, label = "邮箱")
        Spacer(Modifier.height(12.dp))
        AuthTextField(code, { code = it }, label = "邮箱验证码", trailingIcon = {
            CodeSendButton(countdown, sending) { viewModel.sendCode(email) }
        })
        Spacer(Modifier.height(12.dp))
        AuthTextField(password, { password = it }, label = "新密码（至少 8 位）", isPassword = true)
        Spacer(Modifier.height(16.dp))

        if (!passwordValid && password.isNotEmpty()) {
            Text("密码至少 8 位", style = SirenType.Body, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        error?.let {
            Text(it, style = SirenType.Body, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.resetPassword(email, code, PasswordHasher.hash(password)) },
            enabled = email.isNotBlank() && code.isNotBlank() && passwordValid && !submitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (submitting) {
                CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp, color = AccentCyan)
            } else {
                Text("重置密码")
            }
        }
    }

    if (success) {
        AlertDialog(
            onDismissRequest = { onBack() },
            title = { Text("密码已重置") },
            text = { Text("请使用新密码返回登录。") },
            confirmButton = {
                TextButton(onClick = { onBack() }) { Text("返回登录", color = AccentCyan) }
            },
            containerColor = SurfaceDark,
        )
    }
}
