package com.example.musicsiren.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicsiren.domain.model.LyricLine
import com.example.musicsiren.domain.model.Song
import com.example.musicsiren.ui.components.AppPause
import com.example.musicsiren.ui.components.HairlineDivider
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.TextPrimary
import com.example.musicsiren.ui.theme.TextSecondary
import com.example.musicsiren.ui.util.formatClock

/**
 * 歌词页：当前行青蓝高亮 + 自动滚动；无歌词显示「纯音乐」。
 */
@Composable
fun LyricsPage(
    song: Song?,
    lyricsState: LyricsUiState,
    positionMs: Long,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        // 头部
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回封面", tint = TextPrimary)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = song?.name ?: "",
                    style = SirenType.DisplaySans,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song?.artists?.joinToString(" / ") ?: "",
                    style = SirenType.Label,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(formatClock(positionMs), style = SirenType.Clock, color = TextSecondary)
        }
        HairlineDivider()

        when (lyricsState) {
            LyricsUiState.Idle, LyricsUiState.Loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentCyan)
            }
            LyricsUiState.NoLyrics -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("纯音乐", style = SirenType.DisplaySerif.copy(fontSize = 30.sp), color = TextPrimary)
                    Spacer(Modifier.height(10.dp))
                    Text("这首歌没有歌词", style = SirenType.Body, color = TextSecondary)
                }
            }
            is LyricsUiState.Lyrics -> LyricsList(
                lines = lyricsState.lines,
                positionMs = positionMs,
                modifier = Modifier.weight(1f),
            )
        }

        // 底部控制
        Row(
            Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onTogglePlay, modifier = Modifier.size(64.dp)) {
                Icon(
                    imageVector = if (isPlaying) AppPause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    tint = AccentCyan,
                    modifier = Modifier.size(40.dp),
                )
            }
        }
    }
}

@Composable
private fun LyricsList(lines: List<LyricLine>, positionMs: Long, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val currentIndex = lines.indexOfLast { it.timeMs <= positionMs }.coerceAtLeast(0)

    LaunchedEffect(currentIndex) {
        if (currentIndex > 0) listState.animateScrollToItem(currentIndex)
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 36.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(lines) { index, line ->
            val isActive = index == currentIndex
            Text(
                text = line.text,
                textAlign = TextAlign.Center,
                style = if (isActive) {
                    SirenType.Body.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    SirenType.Body.copy(fontSize = 15.sp)
                },
                color = if (isActive) AccentCyan else TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
            )
        }
    }
}
