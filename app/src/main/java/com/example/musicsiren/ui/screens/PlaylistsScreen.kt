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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicsiren.R
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

/** 歌单列表页：新建（+）/ 删除（⋮）歌单，点击进入详情。 */
@Composable
fun PlaylistsScreen(
    container: AppContainer,
    onPlaylistClick: (String) -> Unit,
    viewModel: PlaylistsViewModel = viewModel { PlaylistsViewModel(container.playlistRepository) },
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<Playlist?>(null) }

    Column(Modifier.fillMaxSize().background(Background)) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("歌单", style = SirenType.DisplaySerif, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text("MY PLAYLISTS", style = SirenType.Label, color = TextMuted)
            }
            IconButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, contentDescription = "新建歌单", tint = AccentCyan)
            }
        }
        HairlineDivider()

        if (playlists.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "还没有歌单\n点击右上角 + 创建",
                    style = SirenType.Body,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn {
                items(playlists, key = { it.id }) { pl ->
                    PlaylistRow(
                        playlist = pl,
                        onClick = { onPlaylistClick(pl.id) },
                        onDelete = { deleteTarget = pl },
                    )
                    HairlineDivider()
                }
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("新建歌单") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text("歌单名称") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newName.isNotBlank(),
                    onClick = {
                        viewModel.createPlaylist(newName)
                        newName = ""
                        showCreate = false
                    },
                ) { Text("创建", color = AccentCyan) }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("取消") } },
            containerColor = SurfaceDark,
        )
    }

    deleteTarget?.let { pl ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除歌单") },
            text = { Text("确定删除歌单「${pl.name}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePlaylist(pl.id)
                    deleteTarget = null
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("取消") } },
            containerColor = SurfaceDark,
        )
    }
}

@Composable
private fun PlaylistRow(
    playlist: Playlist,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverImage(playlist.coverUrl, Modifier.size(52.dp))
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = SirenType.DisplaySerif.copy(fontSize = 16.sp),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text("${playlist.songs.size} 首", style = SirenType.Body, color = TextSecondary)
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "更多", tint = TextSecondary)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("删除歌单") },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    },
                )
            }
        }
    }
}
