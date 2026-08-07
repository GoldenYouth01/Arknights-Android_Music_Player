package com.example.musicsiren.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicsiren.R
import com.example.musicsiren.di.AppContainer
import com.example.musicsiren.domain.model.Album
import com.example.musicsiren.domain.model.DownloadStatus
import com.example.musicsiren.domain.model.Song
import com.example.musicsiren.playback.PlaybackViewModel
import com.example.musicsiren.ui.UiState
import com.example.musicsiren.ui.components.CoverImage
import com.example.musicsiren.ui.components.DownloadAffordance
import com.example.musicsiren.ui.components.ErrorState
import com.example.musicsiren.ui.components.HairlineDivider
import com.example.musicsiren.ui.components.LoadingBox
import com.example.musicsiren.ui.components.SongRow
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.AccentTeal
import com.example.musicsiren.ui.theme.Background
import com.example.musicsiren.ui.theme.ScrimBlack
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.SurfaceDark
import com.example.musicsiren.ui.theme.TextPrimary
import com.example.musicsiren.ui.theme.TextSecondary
import com.example.musicsiren.ui.util.formatBytes

/**
 * 专辑详情：模糊封面横幅 + 播放全部 + 歌曲列表（每行带下载入口）。
 * 每次进入重新拉取，保证歌曲列表最新。
 */
@Composable
fun AlbumDetailScreen(
    cid: String,
    container: AppContainer,
    playbackViewModel: PlaybackViewModel,
    onBack: () -> Unit,
    viewModel: AlbumDetailViewModel = viewModel {
        AlbumDetailViewModel(container.sirenRepository, container.downloadRepository, cid)
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val downloads by container.downloadRepository.records.collectAsStateWithLifecycle()
    var pendingDownload by remember { mutableStateOf<Pair<Song, Album>?>(null) }

    Box(Modifier.fillMaxSize().background(Background)) {
        when (val state = uiState) {
            UiState.Loading -> LoadingBox()
            is UiState.Error -> ErrorState(state.message, onRetry = { viewModel.load() })
            is UiState.Data -> {
                val detail = state.data
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    // 模糊封面横幅
                    Box {
                        CoverImage(
                            url = detail.album.coverDeUrl ?: detail.album.coverUrl,
                            modifier = Modifier.fillMaxWidth().height(200.dp).blur(30.dp),
                        )
                        Box(Modifier.matchParentSize().background(ScrimBlack))
                        Row(
                            Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = TextPrimary)
                            }
                            Text("专辑", style = SirenType.DisplaySans, color = TextSecondary)
                        }
                    }
                    // 专辑信息 + 播放全部
                    Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        CoverImage(detail.album.coverUrl, Modifier.size(120.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = detail.album.name,
                                style = SirenType.DisplaySerif,
                                color = TextPrimary,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = detail.album.artistes.joinToString(" / "),
                                style = SirenType.Body,
                                color = AccentTeal,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    playbackViewModel.playQueue(
                                        detail.songs, 0, detail.album.coverUrl, detail.album.name
                                    )
                                },
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.play_all))
                            }
                        }
                    }
                    HairlineDivider()
                    // 歌曲列表
                    detail.songs.forEachIndexed { index, song ->
                        SongRow(
                            song = song,
                            index = index,
                            isActive = playbackViewModel.uiState.value.currentSong?.cid == song.cid,
                            onClick = {
                                playbackViewModel.playQueue(
                                    detail.songs, index, detail.album.coverUrl, detail.album.name
                                )
                            },
                            trailing = {
                                val record = downloads[song.cid]
                                DownloadAffordance(
                                    record = record,
                                    onClick = {
                                        when (record?.status) {
                                            DownloadStatus.DOWNLOADED,
                                            DownloadStatus.ERROR -> viewModel.toggleDownload(song, detail.album)
                                            else -> pendingDownload = song to detail.album
                                        }
                                    },
                                )
                            },
                        )
                        HairlineDivider()
                    }
                    Spacer(Modifier.navigationBarsPadding().height(96.dp))
                }
            }
        }
    }

    // 下载确认（存储告警：每首约 54MB）
    pendingDownload?.let { (song, album) ->
        AlertDialog(
            onDismissRequest = { pendingDownload = null },
            title = { Text(stringResource(R.string.download_confirm_title)) },
            text = {
                val estimate = 54L * 1024 * 1024
                val available = container.downloadRepository.availableBytes()
                val message = buildString {
                    append(stringResource(R.string.download_confirm_message, formatBytes(estimate)))
                    if (available > 0) append("\n可用空间：" + formatBytes(available))
                }
                Text(message, style = SirenType.Body)
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.toggleDownload(song, album)
                    pendingDownload = null
                }) {
                    Text(stringResource(R.string.download), color = AccentCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDownload = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = SurfaceDark,
        )
    }
}
