package com.example.musicsiren.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicsiren.R
import com.example.musicsiren.di.AppContainer
import com.example.musicsiren.domain.model.DownloadRecord
import com.example.musicsiren.domain.model.DownloadStatus
import com.example.musicsiren.domain.model.Song
import com.example.musicsiren.playback.PlaybackViewModel
import com.example.musicsiren.ui.components.CoverImage
import com.example.musicsiren.ui.components.HairlineDivider
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.AccentTeal
import com.example.musicsiren.ui.theme.Background
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.SurfaceDark
import com.example.musicsiren.ui.theme.TextMuted
import com.example.musicsiren.ui.theme.TextPrimary
import com.example.musicsiren.ui.theme.TextSecondary
import com.example.musicsiren.ui.util.formatBytes

/** 下载管理：显示每首的状态 / 大小 / 本地播放 / 删除，顶部统计总用量。 */
@Composable
fun DownloadsScreen(
    container: AppContainer,
    playbackViewModel: PlaybackViewModel,
    viewModel: DownloadsViewModel = viewModel { DownloadsViewModel(container.downloadRepository) },
) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val list = records.values.sortedByDescending { it.completedAt }
    val totalBytes = list.filter { it.status == DownloadStatus.DOWNLOADED }.sumOf { it.totalBytes }
    var deleteTarget by remember { mutableStateOf<DownloadRecord?>(null) }

    Column(Modifier.fillMaxSize().background(Background)) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("下载管理", style = SirenType.DisplaySerif, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "已下载 ${formatBytes(totalBytes)} · 可用 ${formatBytes(container.downloadRepository.availableBytes())}",
                    style = SirenType.Label,
                    color = TextMuted,
                )
            }
        }
        HairlineDivider()

        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "还没有下载内容\n在专辑详情页点击下载图标开始",
                    style = SirenType.Body,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn {
                items(list, key = { it.songCid }) { record ->
                    DownloadRow(
                        record = record,
                        onPlay = {
                            playbackViewModel.playSong(
                                song = Song(
                                    cid = record.songCid,
                                    name = record.songName,
                                    albumCid = record.albumCid,
                                    artists = record.artistNames,
                                    sourceUrl = record.sourceUrl,
                                    lyricUrl = null,
                                ),
                                coverUrl = record.coverUrl,
                                albumName = record.albumName,
                            )
                        },
                        onDelete = { deleteTarget = record },
                        onRetry = { viewModel.retry(record) },
                    )
                    HairlineDivider()
                }
            }
        }
    }

    deleteTarget?.let { record ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_message, formatBytes(record.totalBytes))) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(record.songCid)
                    deleteTarget = null
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = SurfaceDark,
        )
    }
}

@Composable
private fun DownloadRow(
    record: DownloadRecord,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverImage(record.coverUrl, Modifier.size(48.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = record.songName,
                style = SirenType.Body.copy(fontWeight = FontWeight.Medium),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = record.artistNames.joinToString(" / "),
                style = SirenType.Label,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            when (record.status) {
                DownloadStatus.DOWNLOADED -> Text(
                    "${formatBytes(record.totalBytes)} · 已下载",
                    style = SirenType.Label,
                    color = AccentTeal,
                )
                DownloadStatus.DOWNLOADING -> Text(
                    progressText(record),
                    style = SirenType.Label,
                    color = AccentCyan,
                )
                DownloadStatus.PENDING -> Text("等待下载…", style = SirenType.Label, color = TextMuted)
                DownloadStatus.ERROR -> Text(
                    "失败：${record.error ?: "未知错误"}",
                    style = SirenType.Label,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        when (record.status) {
            DownloadStatus.DOWNLOADED -> IconButton(onClick = onPlay) {
                Icon(Icons.Default.PlayArrow, contentDescription = "播放", tint = AccentCyan)
            }
            DownloadStatus.DOWNLOADING -> CircularProgressIndicator(
                progress = { if (record.totalBytes > 0) (record.progressBytes.toFloat() / record.totalBytes).coerceIn(0f, 1f) else 0f },
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = AccentCyan,
            )
            DownloadStatus.PENDING -> Text("等待中", style = SirenType.Label, color = TextMuted)
            DownloadStatus.ERROR -> IconButton(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = "重试", tint = TextSecondary)
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "删除", tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

private fun progressText(record: DownloadRecord): String {
    val percent = if (record.totalBytes > 0) {
        record.progressBytes * 100 / record.totalBytes
    } else {
        0
    }
    return "下载中 $percent%"
}
