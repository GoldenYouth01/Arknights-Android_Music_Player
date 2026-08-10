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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicsiren.di.AppContainer
import com.example.musicsiren.domain.model.HistoryEntry
import com.example.musicsiren.playback.PlaybackViewModel
import com.example.musicsiren.ui.components.CoverImage
import com.example.musicsiren.ui.components.HairlineDivider
import com.example.musicsiren.ui.theme.Background
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.TextMuted
import com.example.musicsiren.ui.theme.TextPrimary
import com.example.musicsiren.ui.theme.TextSecondary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 播放历史：最近播放的歌曲，点行直接播放。 */
@Composable
fun HistoryScreen(
    container: AppContainer,
    playbackViewModel: PlaybackViewModel,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel {
        HistoryViewModel(
            container.historyRepository,
            container.cloudRepository,
            container.sirenRepository,
            playbackViewModel,
        )
    },
) {
    val history by viewModel.history.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(Background)) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = TextPrimary)
            }
            Column(Modifier.weight(1f)) {
                Text("播放历史", style = SirenType.DisplaySerif, color = TextPrimary)
                Text("RECENTLY PLAYED", style = SirenType.Label, color = TextMuted)
            }
        }
        HairlineDivider()

        if (history.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "还没有播放记录\n播放过的歌曲会出现在这里",
                    style = SirenType.Body,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn {
                items(history, key = { it.cid }) { entry ->
                    HistoryRow(entry = entry, onClick = { viewModel.play(entry) })
                    HairlineDivider()
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverImage(entry.coverUrl, Modifier.size(48.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.name,
                style = SirenType.DisplaySerif.copy(fontSize = 16.sp),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(entry.artists.joinToString(" / "), style = SirenType.Body, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(12.dp))
        Text(formatPlayedAt(entry.playedAt), style = SirenType.Clock, color = TextMuted)
    }
}

private val playedAtFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault())

private fun formatPlayedAt(epochMs: Long): String =
    if (epochMs > 0L) playedAtFormatter.format(Instant.ofEpochMilli(epochMs)) else ""
