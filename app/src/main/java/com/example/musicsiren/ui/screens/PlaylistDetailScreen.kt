package com.example.musicsiren.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicsiren.R
import com.example.musicsiren.di.AppContainer
import com.example.musicsiren.domain.model.Song
import com.example.musicsiren.playback.PlaybackViewModel
import com.example.musicsiren.ui.components.HairlineDivider
import com.example.musicsiren.ui.components.SongRow
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.Background
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.SurfaceDark
import com.example.musicsiren.ui.theme.TextPrimary
import com.example.musicsiren.ui.theme.TextSecondary

/** 歌单详情页：播放全部 + 歌曲列表（⋮ 从歌单移除）+ 改名/删除 + 分享。 */
@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    container: AppContainer,
    playbackViewModel: PlaybackViewModel,
    onBack: () -> Unit,
    onNavigateLogin: () -> Unit,
    viewModel: PlaylistDetailViewModel = viewModel {
        PlaylistDetailViewModel(container.playlistRepository, container.cloudRepository, playlistId)
    },
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val shareCode by viewModel.shareCode.collectAsStateWithLifecycle()
    val shareError by viewModel.shareError.collectAsStateWithLifecycle()
    val sharing by viewModel.sharing.collectAsStateWithLifecycle()
    val playlist = playlists.find { it.id == playlistId }
    var showRename by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }
    var showLoginPrompt by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(Modifier.fillMaxSize().background(Background)) {
        if (playlist == null) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            ) {
                Text("歌单不存在或已删除", style = SirenType.Body, color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onBack) { Text(stringResource(R.string.back)) }
            }
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // 头部
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = TextPrimary)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = playlist.name,
                            style = SirenType.DisplaySerif,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text("${playlist.songs.size} 首歌曲", style = SirenType.Label, color = TextSecondary)
                    }
                    IconButton(onClick = {
                        renameValue = playlist.name
                        showRename = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "改名", tint = TextSecondary)
                    }
                    IconButton(onClick = {
                        if (viewModel.isLoggedIn) viewModel.share() else showLoginPrompt = true
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "分享歌单", tint = TextSecondary)
                    }
                    IconButton(onClick = {
                        viewModel.delete()
                        onBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除歌单", tint = TextSecondary)
                    }
                }
                // 播放全部
                Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Button(onClick = {
                        playbackViewModel.playQueue(viewModel.toSongs(), 0, playlist.coverUrl, playlist.name)
                    }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.play_all))
                    }
                }
                HairlineDivider()

                if (playlist.songs.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "歌单是空的\n在歌曲行的 ⋮ 菜单中选择「加入歌单」",
                            style = SirenType.Body,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    playlist.songs.forEachIndexed { index, ps ->
                        val song = Song(ps.cid, ps.name, ps.albumCid, ps.artists, null, null)
                        SongRow(
                            song = song,
                            index = index,
                            isActive = playbackViewModel.uiState.value.currentSong?.cid == ps.cid,
                            onClick = {
                                playbackViewModel.playQueue(viewModel.toSongs(), index, playlist.coverUrl, playlist.name)
                            },
                            coverUrl = ps.coverUrl,
                            menuItems = { dismiss ->
                                DropdownMenuItem(
                                    text = { Text("从歌单移除") },
                                    onClick = {
                                        dismiss()
                                        viewModel.removeSong(ps.cid)
                                    },
                                )
                            },
                        )
                        HairlineDivider()
                    }
                }
                Spacer(Modifier.navigationBarsPadding().height(96.dp))
            }
        }
    }

    if (showRename) {
        AlertDialog(
            onDismissRequest = { showRename = false },
            title = { Text("歌单改名") },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = renameValue.isNotBlank(),
                    onClick = {
                        viewModel.rename(renameValue)
                        showRename = false
                    },
                ) { Text("保存", color = AccentCyan) }
            },
            dismissButton = { TextButton(onClick = { showRename = false }) { Text("取消") } },
            containerColor = SurfaceDark,
        )
    }

    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = { showLoginPrompt = false },
            title = { Text("需要登录") },
            text = { Text("登录后才能分享歌单到云端。") },
            confirmButton = {
                TextButton(onClick = {
                    showLoginPrompt = false
                    onNavigateLogin()
                }) { Text("去登录", color = AccentCyan) }
            },
            dismissButton = { TextButton(onClick = { showLoginPrompt = false }) { Text("取消") } },
            containerColor = SurfaceDark,
        )
    }

    shareCode?.let { code ->
        AlertDialog(
            onDismissRequest = { viewModel.clearShare() },
            title = { Text("分享歌单") },
            text = {
                Column {
                    Text("把以下分享码发给好友，对方在「导入分享码」粘贴即可保存此歌单：")
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = code,
                        style = SirenType.DisplaySans.copy(fontSize = 24.sp),
                        color = AccentCyan,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("shareCode", code))
                    Toast.makeText(context, "已复制分享码", Toast.LENGTH_SHORT).show()
                }) { Text("复制", color = AccentCyan) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearShare() }) { Text(if (sharing) "获取中…" else "关闭") }
            },
            containerColor = SurfaceDark,
        )
    }

    shareError?.let { err ->
        AlertDialog(
            onDismissRequest = { viewModel.clearShare() },
            title = { Text("分享失败") },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearShare() }) { Text("知道了", color = AccentCyan) }
            },
            containerColor = SurfaceDark,
        )
    }
}
