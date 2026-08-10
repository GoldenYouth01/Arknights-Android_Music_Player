package com.example.musicsiren.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicsiren.domain.model.LyricLine
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.TextPrimary
import com.example.musicsiren.ui.theme.TextSecondary

/**
 * 歌词内容（播放页页1，仅渲染在封面同款正方形区域内）：当前行青蓝高亮 + 自动滚动；
 * 无歌词显示「纯音乐」。头部/进度/控制由外层播放页共享。
 */
@Composable
fun LyricsContent(
    lyricsState: LyricsUiState,
    positionMs: Long,
    modifier: Modifier = Modifier,
) {
    when (lyricsState) {
        LyricsUiState.Idle, LyricsUiState.Loading -> Box(modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentCyan)
        }
        LyricsUiState.NoLyrics -> Box(modifier, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("纯音乐", style = SirenType.DisplaySerif.copy(fontSize = 30.sp), color = TextPrimary)
                Spacer(Modifier.height(10.dp))
                Text("这首歌没有歌词", style = SirenType.Body, color = TextSecondary)
            }
        }
        is LyricsUiState.Lyrics -> LyricsList(
            lines = lyricsState.lines,
            positionMs = positionMs,
            modifier = modifier,
        )
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
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(lines) { index, line ->
            val isActive = index == currentIndex
            Text(
                text = line.text,
                textAlign = TextAlign.Center,
                style = if (isActive) {
                    SirenType.Body.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
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
