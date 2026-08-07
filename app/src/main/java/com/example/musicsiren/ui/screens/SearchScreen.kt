package com.example.musicsiren.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicsiren.R
import com.example.musicsiren.data.search.CatalogState
import com.example.musicsiren.di.AppContainer
import com.example.musicsiren.domain.model.Song
import com.example.musicsiren.playback.PlaybackViewModel
import com.example.musicsiren.ui.components.AlbumRow
import com.example.musicsiren.ui.components.HairlineDivider
import com.example.musicsiren.ui.components.PlaylistPicker
import com.example.musicsiren.ui.components.SongRow
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.Background
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.SurfaceDark
import com.example.musicsiren.ui.theme.TextMuted
import com.example.musicsiren.ui.theme.TextPrimary
import com.example.musicsiren.ui.theme.TextSecondary

/**
 * 搜索：歌曲走本地 SongCatalog（可刷新目录），专辑走 /api/search。
 */
@Composable
fun SearchScreen(
    container: AppContainer,
    playbackViewModel: PlaybackViewModel,
    onAlbumClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel {
        SearchViewModel(container.songCatalog, container.sirenRepository)
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val catalogState by container.songCatalog.state.collectAsStateWithLifecycle()
    val playlists by container.playlistRepository.playlists.collectAsStateWithLifecycle()
    var pendingAddSong by remember { mutableStateOf<Song?>(null) }

    Column(Modifier.fillMaxSize().background(Background).imePadding()) {
        // 搜索框
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(color = SurfaceDark, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f)) {
                Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.onQueryChange(it) },
                        modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                        textStyle = SirenType.Body.copy(color = TextPrimary),
                        singleLine = true,
                        cursorBrush = SolidColor(AccentCyan),
                    )
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "清空", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
        // 刷新目录 + 索引状态
        Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { viewModel.refreshCatalog() }) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentCyan)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.refresh_catalog), style = SirenType.Label, color = TextSecondary)
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = when (val s = catalogState) {
                    is CatalogState.Ready -> "已索引 ${s.songCount} 首"
                    CatalogState.Loading -> "正在索引…"
                    is CatalogState.Error -> "目录加载失败"
                    CatalogState.Idle -> ""
                },
                style = SirenType.Label,
                color = TextMuted,
                modifier = Modifier.padding(end = 16.dp),
            )
        }
        HairlineDivider()

        if (uiState.query.isBlank()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.search_hint),
                    style = SirenType.Body,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn {
                if (uiState.songs.isNotEmpty()) {
                    item { SectionTitle("歌曲") }
                    items(uiState.songs, key = { it.cid }) { song ->
                        SongRow(
                            song = song,
                            index = -1,
                            isActive = playbackViewModel.uiState.value.currentSong?.cid == song.cid,
                            onClick = { playbackViewModel.playQueue(uiState.songs, uiState.songs.indexOf(song), null, null) },
                            menuItems = { dismiss ->
                                DropdownMenuItem(
                                    text = { Text("加入歌单") },
                                    onClick = {
                                        dismiss()
                                        pendingAddSong = song
                                    },
                                )
                            },
                        )
                        HairlineDivider()
                    }
                }
                if (uiState.albums.isNotEmpty()) {
                    item { SectionTitle("专辑") }
                    items(uiState.albums, key = { it.cid }) { album ->
                        AlbumRow(album, onClick = { onAlbumClick(album.cid) })
                        HairlineDivider()
                    }
                }
                if (uiState.songs.isEmpty() && uiState.albums.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("未找到结果", style = SirenType.Body, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }

    // 加入歌单选择器
    pendingAddSong?.let { song ->
        PlaylistPicker(
            playlists = playlists,
            onAddToPlaylist = { id ->
                container.playlistRepository.addSong(id, song)
                pendingAddSong = null
            },
            onCreateNew = { name ->
                container.playlistRepository.createPlaylist(name, song)
                pendingAddSong = null
            },
            onDismiss = { pendingAddSong = null },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = SirenType.DisplaySans,
        color = TextSecondary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    )
}
