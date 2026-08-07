package com.example.musicsiren.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicsiren.domain.model.DownloadRecord
import com.example.musicsiren.domain.model.DownloadStatus
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.TextSecondary

/** 歌曲行末尾的下载状态入口：未下载→下载；下载中/等待→进度环；已下载→完成；失败→重试。 */
@Composable
fun DownloadAffordance(
    record: DownloadRecord?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = record?.status
    IconButton(onClick = onClick, modifier = modifier.size(34.dp)) {
        when (status) {
            DownloadStatus.DOWNLOADED -> Icon(
                imageVector = AppDownloadDone,
                contentDescription = "已下载",
                tint = AccentCyan,
                modifier = Modifier.size(18.dp),
            )
            DownloadStatus.ERROR -> Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "重试下载",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp),
            )
            DownloadStatus.PENDING, DownloadStatus.DOWNLOADING -> {
                val progress = if ((record?.totalBytes ?: 0L) > 0L) {
                    (record.progressBytes.toFloat() / record.totalBytes).coerceIn(0f, 1f)
                } else {
                    0f
                }
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = AccentCyan,
                )
            }
            null -> Icon(
                imageVector = AppDownload,
                contentDescription = "下载",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
