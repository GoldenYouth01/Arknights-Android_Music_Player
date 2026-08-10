package com.example.musicsiren.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.musicsiren.ui.components.AvatarImage
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.SurfaceDark
import com.example.musicsiren.ui.theme.TextPrimary
import com.example.musicsiren.ui.theme.TextSecondary

/** 账号信息页：展示并修改头像 / 昵称（邮箱只读）。点头像可经系统相册 → 正方形裁剪 → 上传。 */
@Composable
fun AccountInfoScreen(
    container: AppContainer,
    onBack: () -> Unit,
    viewModel: AccountInfoViewModel = viewModel { AccountInfoViewModel(container.authRepository) },
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var nickname by remember(session?.nickname) { mutableStateOf(session?.nickname ?: "") }
    var pendingAvatar by remember { mutableStateOf<Uri?>(null) }
    var showCrop by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            pendingAvatar = uri
            showCrop = true
        }
    }

    if (session == null) {
        AuthScaffold(title = "账号信息", subtitle = "PROFILE", onBack = onBack) {
            Text("未登录", style = SirenType.Body, color = TextSecondary)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onBack) { Text("返回") }
        }
        return
    }

    AuthScaffold(title = "账号信息", subtitle = "PROFILE", onBack = onBack) {
        if (busy) {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = AccentCyan, trackColor = SurfaceDark)
            Spacer(Modifier.height(12.dp))
        }

        // 头像
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AvatarImage(url = session!!.avatarUrl, nickname = session!!.nickname, size = 96.dp)
        }
        Spacer(Modifier.height(12.dp))
        TextButton(
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) { Text("更换头像", color = AccentCyan) }
        Spacer(Modifier.height(24.dp))

        // 邮箱（只读）
        Text("邮箱", style = SirenType.Label, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(session!!.email, style = SirenType.Body, color = TextPrimary)
        Spacer(Modifier.height(20.dp))

        // 昵称
        Text("昵称", style = SirenType.Label, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        AuthTextField(nickname, { nickname = it }, label = "昵称（≤64 字）")
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { viewModel.saveNickname(nickname) },
            enabled = nickname.isNotBlank() && !busy,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("保存昵称") }
    }

    if (showCrop && pendingAvatar != null) {
        AvatarCropScreen(
            uri = pendingAvatar!!,
            onBack = { showCrop = false },
            onConfirm = { cropped ->
                showCrop = false
                viewModel.uploadAvatar(cropped)
            },
        )
    }

    message?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            title = { Text("提示") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessage() }) { Text("知道了", color = AccentCyan) }
            },
            containerColor = SurfaceDark,
        )
    }
}
