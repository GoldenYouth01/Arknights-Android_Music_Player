package com.example.musicsiren.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicsiren.di.AppContainer
import com.example.musicsiren.domain.model.AuthSession
import com.example.musicsiren.ui.components.HairlineDivider
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.Background
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.SurfaceDark
import com.example.musicsiren.ui.theme.TextMuted
import com.example.musicsiren.ui.theme.TextPrimary
import com.example.musicsiren.ui.theme.TextSecondary

/** 账号 tab：未登录展示登录/注册入口；已登录展示信息 + 云端同步动作。 */
@Composable
fun AccountScreen(
    container: AppContainer,
    onNavigateLogin: () -> Unit,
    onNavigateRegister: () -> Unit,
    onNavigateCloudPlaylists: () -> Unit,
    onNavigateHistory: () -> Unit,
    viewModel: AccountViewModel = viewModel {
        AccountViewModel(container.authRepository, container.cloudRepository)
    },
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var showImport by remember { mutableStateOf(false) }
    var importCode by remember { mutableStateOf("") }
    var confirmDownload by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Background)) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("账号", style = SirenType.DisplaySerif, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text("ACCOUNT", style = SirenType.Label, color = TextMuted)
            }
        }
        HairlineDivider()

        if (busy) {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = AccentCyan, trackColor = SurfaceDark)
        }

        if (session == null) {
            LoggedOutPanel(
                onNavigateLogin = onNavigateLogin,
                onNavigateRegister = onNavigateRegister,
            )
        } else {
            LoggedInContent(
                session = session!!,
                busy = busy,
                onUpload = viewModel::upload,
                onDownload = { confirmDownload = true },
                onImport = { showImport = true },
                onNavigateCloudPlaylists = onNavigateCloudPlaylists,
                onNavigateHistory = onNavigateHistory,
                onLogout = { confirmLogout = true },
            )
        }
    }

    if (showImport) {
        AlertDialog(
            onDismissRequest = { showImport = false },
            title = { Text("导入分享歌单") },
            text = {
                OutlinedTextField(
                    value = importCode,
                    onValueChange = { importCode = it },
                    placeholder = { Text("输入 8 位分享码") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = importCode.isNotBlank(),
                    onClick = {
                        showImport = false
                        viewModel.importShare(importCode)
                        importCode = ""
                    },
                ) { Text("导入", color = AccentCyan) }
            },
            dismissButton = { TextButton(onClick = { showImport = false }) { Text("取消") } },
            containerColor = SurfaceDark,
        )
    }

    if (confirmDownload) {
        AlertDialog(
            onDismissRequest = { confirmDownload = false },
            title = { Text("从云端下载") },
            text = { Text("将用云端歌单覆盖本地歌单，本地未同步的改动会丢失。确定继续？") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDownload = false
                    viewModel.download()
                }) { Text("覆盖下载", color = AccentCyan) }
            },
            dismissButton = { TextButton(onClick = { confirmDownload = false }) { Text("取消") } },
            containerColor = SurfaceDark,
        )
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("退出登录") },
            text = { Text("退出后本地歌单仍保留，但不再自动上传播放历史。确定退出？") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    viewModel.logout()
                }) { Text("退出", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("取消") } },
            containerColor = SurfaceDark,
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

@Composable
private fun LoggedOutPanel(
    onNavigateLogin: () -> Unit,
    onNavigateRegister: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Text("登录后可将歌单与播放历史\n同步到云端", style = SirenType.Body, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onNavigateLogin, modifier = Modifier.fillMaxWidth()) {
            Text("登录")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onNavigateRegister,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("注册新账号")
        }
    }
}

@Composable
private fun LoggedInContent(
    session: AuthSession,
    busy: Boolean,
    onUpload: () -> Unit,
    onDownload: () -> Unit,
    onImport: () -> Unit,
    onNavigateCloudPlaylists: () -> Unit,
    onNavigateHistory: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        // 用户信息头部
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(AccentCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = session.nickname?.firstOrNull()?.uppercase() ?: "♪",
                    style = SirenType.DisplaySerif,
                    color = AccentCyan,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = session.nickname?.ifBlank { "用户" } ?: "用户",
                    style = SirenType.DisplaySerif,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(session.email, style = SirenType.Body, color = TextSecondary)
            }
        }
        HairlineDivider()

        AccountActionRow("云端歌单", onNavigateCloudPlaylists)
        HairlineDivider()
        AccountActionRow("播放历史", onNavigateHistory)
        HairlineDivider()
        AccountActionRow("上传歌单到云端", onUpload, enabled = !busy)
        HairlineDivider()
        AccountActionRow("从云端下载歌单", onDownload, enabled = !busy)
        HairlineDivider()
        AccountActionRow("导入分享歌单", onImport, enabled = !busy)
        HairlineDivider()
        AccountActionRow("退出登录", onLogout, danger = true)
        HairlineDivider()
    }
}

@Composable
private fun AccountActionRow(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    danger: Boolean = false,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = SirenType.Body,
            color = if (danger) MaterialTheme.colorScheme.error else TextPrimary,
        )
    }
}
