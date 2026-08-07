package com.example.musicsiren.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.musicsiren.domain.model.Playlist
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.SurfaceDark
import com.example.musicsiren.ui.theme.TextPrimary
import com.example.musicsiren.ui.theme.TextSecondary

/**
 * "加入歌单"选择器对话框：列出全部歌单点击即加入；底部"新建歌单"。
 * [onCreateNew] 由上层负责创建歌单并把待加入歌曲放进去。
 */
@Composable
fun PlaylistPicker(
    playlists: List<Playlist>,
    onAddToPlaylist: (String) -> Unit,
    onCreateNew: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

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
                        onCreateNew(newName)
                        showCreate = false
                        newName = ""
                    },
                ) { Text("创建", color = AccentCyan) }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("取消") } },
            containerColor = SurfaceDark,
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("加入歌单") },
        text = {
            Column {
                if (playlists.isEmpty()) {
                    Text("还没有歌单，先创建一个吧", style = SirenType.Body, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                }
                playlists.forEach { pl ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onAddToPlaylist(pl.id) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CoverImage(pl.coverUrl, Modifier.size(40.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = pl.name,
                                style = SirenType.Body,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text("${pl.songs.size} 首", style = SirenType.Label, color = TextSecondary)
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                }
                HairlineDivider()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { showCreate = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("新建歌单", style = SirenType.Body, color = AccentCyan)
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        containerColor = SurfaceDark,
    )
}
