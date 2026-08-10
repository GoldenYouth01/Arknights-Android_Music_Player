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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicsiren.di.AppContainer
import com.example.musicsiren.domain.model.Playlist
import com.example.musicsiren.ui.components.CoverImage
import com.example.musicsiren.ui.components.HairlineDivider
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.Background
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.SurfaceDark
import com.example.musicsiren.ui.theme.TextMuted
import com.example.musicsiren.ui.theme.TextPrimary
import com.example.musicsiren.ui.theme.TextSecondary

/** 云端歌单页：本地歌单列表（带同步状态）+ 全量上传/下载/导入分享码。 */
@Composable
fun CloudPlaylistsScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onPlaylistClick: (String) -> Unit,
    viewModel: CloudSyncViewModel = viewModel {
        CloudSyncViewModel(container.cloudRepository, container.playlistRepository)
    },
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var showImport by remember { mutableStateOf(false) }
    var importCode by remember { mutableStateOf("") }
    var confirmDownload by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Background)) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = TextPrimary)
            }
            Column(Modifier.weight(1f)) {
                Text("云端歌单", style = SirenType.DisplaySerif, color = TextPrimary)
                Text("CLOUD PLAYLISTS", style = SirenType.Label, color = TextMuted)
            }
        }
        HairlineDivider()

        // 同步操作
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            ActionChip("上传到云端", enabled = !busy, onClick = viewModel::upload)
            Spacer(Modifier.width(8.dp))
            ActionChip("从云端下载", enabled = !busy, onClick = { confirmDownload = true })
            Spacer(Modifier.width(8.dp))
            ActionChip("导入分享码", enabled = !busy, onClick = { showImport = true })
        }
        if (busy) {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = AccentCyan, trackColor = SurfaceDark)
        }
        HairlineDivider()

        if (playlists.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "还没有歌单\n在「歌单」页创建后，点「上传到云端」同步",
                    style = SirenType.Body,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn {
                items(playlists, key = { it.id }) { pl ->
                    CloudPlaylistRow(playlist = pl, onClick = { onPlaylistClick(pl.id) })
                    HairlineDivider()
                }
            }
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
private fun ActionChip(text: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        color = SurfaceDark,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
    ) {
        Text(text, style = SirenType.Body, color = if (enabled) AccentCyan else TextMuted, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
    }
}

@Composable
private fun CloudPlaylistRow(playlist: Playlist, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverImage(playlist.coverUrl, Modifier.size(48.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = SirenType.DisplaySerif.copy(fontSize = 16.sp),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${playlist.songs.size} 首 · ${if (playlist.cloudId != null) "已同步" else "未同步"}",
                style = SirenType.Body,
                color = if (playlist.cloudId != null) TextSecondary else TextMuted,
            )
        }
    }
}
