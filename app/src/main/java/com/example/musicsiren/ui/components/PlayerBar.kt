package com.example.musicsiren.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.musicsiren.playback.PlaybackUiState
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.PlayerBarColor
import com.example.musicsiren.ui.theme.ProgressFill
import com.example.musicsiren.ui.theme.ProgressTrack
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.TextPrimary
import com.example.musicsiren.ui.theme.TextSecondary

/**
 * 底部固定播放条（官网高度约 6em）：顶部 hairline 进度 + 封面 + 标题/艺人 + 传输控制。
 */
@Composable
fun PlayerBar(
    state: PlaybackUiState,
    onTap: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = PlayerBarColor, modifier = modifier.fillMaxWidth()) {
        Column {
            // 顶部细线进度
            Box(Modifier.fillMaxWidth().height(2.dp).background(ProgressTrack)) {
                val fraction = if (state.durationMs > 0L) {
                    (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f)
                } else {
                    0f
                }
                Box(Modifier.fillMaxWidth(fraction).height(2.dp).background(ProgressFill))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onTap)
                    .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CoverImage(
                    url = state.coverUrl,
                    modifier = Modifier.size(46.dp),
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = state.currentSong?.name ?: "",
                        style = SirenType.Body.copy(fontWeight = FontWeight.Medium),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = state.currentSong?.artists?.joinToString(" / ") ?: "",
                        style = SirenType.Label,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onPrevious) {
                    Icon(AppSkipPrevious, contentDescription = "上一首", tint = TextPrimary)
                }
                IconButton(onClick = onTogglePlay) {
                    Icon(
                        imageVector = if (state.isPlaying) AppPause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "暂停" else "播放",
                        tint = AccentCyan,
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(AppSkipNext, contentDescription = "下一首", tint = TextPrimary)
                }
                IconButton(onClick = onOpenDrawer) {
                    Icon(AppQueueMusic, contentDescription = "播放队列", tint = TextSecondary)
                }
            }
        }
    }
}
