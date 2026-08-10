package com.example.musicsiren.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.musicsiren.util.PasswordHasher

/** 注册页：邮箱 + 验证码（倒计时）+ 密码 + 可选昵称。 */
@Composable
fun RegisterScreen(
    container: AppContainer,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = viewModel { RegisterViewModel(container.authRepository) },
) {
    val sending by viewModel.sending.collectAsStateWithLifecycle()
    val submitting by viewModel.submitting.collectAsStateWithLifecycle()
    val countdown by viewModel.countdown.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    val passwordValid = password.length >= 8
    // 防呆：昵称与密码相同（自动填充/误输入把密码填进昵称栏）应阻止
    val nicknameConflict = nickname.isNotBlank() && nickname == password

    AuthScaffold(title = "注册", subtitle = "SIGN UP", onBack = onBack) {
        AuthTextField(email, { email = it }, label = "邮箱")
        Spacer(Modifier.height(12.dp))
        AuthTextField(code, { code = it }, label = "邮箱验证码", trailingIcon = {
            CodeSendButton(countdown, sending) { viewModel.sendCode(email) }
        })
        Spacer(Modifier.height(12.dp))
        AuthTextField(password, { password = it }, label = "密码（至少 8 位）", isPassword = true)
        Spacer(Modifier.height(12.dp))
        AuthTextField(nickname, { nickname = it }, label = "昵称（可选）")
        Spacer(Modifier.height(16.dp))

        if (!passwordValid && password.isNotEmpty()) {
            Text("密码至少 8 位", style = SirenType.Body, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        if (nicknameConflict) {
            Text("昵称不能与密码相同", style = SirenType.Body, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        error?.let {
            Text(it, style = SirenType.Body, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                viewModel.register(
                    email = email,
                    passwordHash = PasswordHasher.hash(password),
                    code = code,
                    nickname = nickname.ifBlank { null },
                    onSuccess = onBack,
                )
            },
            enabled = email.isNotBlank() && code.isNotBlank() && passwordValid && !nicknameConflict && !submitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (submitting) {
                CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp, color = AccentCyan)
            } else {
                Text("注册并登录")
            }
        }
    }
}
