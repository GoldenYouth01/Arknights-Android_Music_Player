package com.example.musicsiren.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.example.musicsiren.playback.PlaybackUiState
import com.example.musicsiren.ui.components.AppPause
import com.example.musicsiren.ui.components.AppRepeat
import com.example.musicsiren.ui.components.AppRepeatOne
import com.example.musicsiren.ui.components.AppShuffle
import com.example.musicsiren.ui.components.AppSkipNext
import com.example.musicsiren.ui.components.AppSkipPrevious
import com.example.musicsiren.ui.components.CoverImage
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.AccentTeal
import com.example.musicsiren.ui.theme.ProgressFill
import com.example.musicsiren.ui.theme.ProgressTrack
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.TextPrimary
import com.example.musicsiren.ui.theme.TextSecondary
import com.example.musicsiren.ui.util.formatClock
import kotlinx.coroutines.launch

/**
 * 全屏播放页：横向翻页 —— 页0 封面/进度/控制；页1 歌词（左滑进入，右滑返回）。
 * 模糊封面背景与蒙层两层页面共用。
 */
@Composable
fun NowPlayingScreen(
    state: PlaybackUiState,
    lyricsViewModel: LyricsViewModel,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lyricsUiState by lyricsViewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()

    // 返回：歌词页可见时先回封面页；否则关闭播放页
    BackHandler(enabled = true) {
        if (pagerState.currentPage == 1) {
            scope.launch { pagerState.animateScrollToPage(0) }
        } else {
            onBack()
        }
    }

    Box(modifier.fillMaxSize()) {
        // 模糊背景（两页共用）
        CoverImage(
            url = state.coverUrl,
            modifier = Modifier.fillMaxSize().blur(80.dp),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.65f),
                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.85f),
                    )
                )
            )
        )

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> PlayerPage(
                    state = state,
                    onBack = onBack,
                    onTogglePlay = onTogglePlay,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeek = onSeek,
                    onToggleShuffle = onToggleShuffle,
                    onCycleRepeat = onCycleRepeat,
                )
                else -> LyricsPage(
                    song = state.currentSong,
                    lyricsState = lyricsUiState,
                    positionMs = state.positionMs,
                    isPlaying = state.isPlaying,
                    onBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                    onTogglePlay = onTogglePlay,
                )
            }
        }
    }
}

/** 页0：封面 + 标题 + 进度 + 传输控制 + 随机/循环。 */
@Composable
private fun PlayerPage(
    state: PlaybackUiState,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = TextPrimary)
            }
            Text(
                text = "正在播放",
                style = SirenType.DisplaySans,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(48.dp))
        }
        Spacer(Modifier.weight(0.3f))
        CoverImage(
            url = state.coverUrl,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.weight(0.15f))
        Text(
            text = state.currentSong?.name ?: "",
            style = SirenType.DisplaySerif.copy(fontSize = 26.sp),
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.currentSong?.artists?.joinToString(" / ") ?: "",
            style = SirenType.Body,
            color = AccentTeal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(0.15f))

        // 进度条 + 时钟读数
        val durationMs = state.durationMs.coerceAtLeast(1L)
        var dragPosition by remember { mutableStateOf<Float?>(null) }
        val displayPosition = dragPosition ?: state.positionMs.toFloat()

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatClock(state.positionMs), style = SirenType.Clock, color = TextSecondary)
            Text(formatClock(durationMs), style = SirenType.Clock, color = TextSecondary)
        }
        Slider(
            value = displayPosition.coerceIn(0f, durationMs.toFloat()),
            onValueChange = { dragPosition = it },
            onValueChangeFinished = {
                dragPosition?.let { onSeek(it.toLong()) }
                dragPosition = null
            },
            valueRange = 0f..durationMs.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = AccentCyan,
                activeTrackColor = ProgressFill,
                inactiveTrackColor = ProgressTrack,
                disabledActiveTrackColor = ProgressFill,
                disabledInactiveTrackColor = ProgressTrack,
            ),
        )

        // 传输控制 + 播放模式
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onToggleShuffle, modifier = Modifier.size(48.dp)) {
                Icon(
                    AppShuffle,
                    contentDescription = "随机播放",
                    tint = if (state.shuffleEnabled) AccentCyan else TextSecondary,
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(onClick = onPrevious, modifier = Modifier.size(56.dp)) {
                Icon(AppSkipPrevious, contentDescription = "上一首", tint = TextPrimary, modifier = Modifier.size(36.dp))
            }
            IconButton(onClick = onTogglePlay, modifier = Modifier.size(88.dp)) {
                Icon(
                    imageVector = if (state.isPlaying) AppPause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlaying) "暂停" else "播放",
                    tint = AccentCyan,
                    modifier = Modifier.size(56.dp),
                )
            }
            IconButton(onClick = onNext, modifier = Modifier.size(56.dp)) {
                Icon(AppSkipNext, contentDescription = "下一首", tint = TextPrimary, modifier = Modifier.size(36.dp))
            }
            IconButton(onClick = onCycleRepeat, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = if (state.repeatMode == Player.REPEAT_MODE_ONE) AppRepeatOne else AppRepeat,
                    contentDescription = "循环播放",
                    tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) AccentCyan else TextSecondary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}
