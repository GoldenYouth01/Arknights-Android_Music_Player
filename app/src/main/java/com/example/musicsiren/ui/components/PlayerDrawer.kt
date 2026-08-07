package com.example.musicsiren.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicsiren.playback.PlaybackUiState
import com.example.musicsiren.ui.theme.DrawerBlack
import com.example.musicsiren.ui.theme.HairlineWhite
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.SurfaceDark
import com.example.musicsiren.ui.theme.TextPrimary

/**
 * 上滑播放队列抽屉：半透明黑色蒙层 + 底部队列面板，激活行青蓝高亮。
 */
@Composable
fun PlayerDrawer(
    state: PlaybackUiState,
    onDismiss: () -> Unit,
    onSelectSong: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().background(DrawerBlack).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onDismiss,
        ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(SurfaceDark)
                .navigationBarsPadding(),
        ) {
            // 顶部把手
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(36.dp)
                    .height(3.dp)
                    .background(HairlineWhite)
            )
            Text(
                text = "播放队列",
                style = SirenType.DisplaySans,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            HairlineDivider()
            LazyColumn(Modifier.heightIn(max = 380.dp)) {
                itemsIndexed(state.queue, key = { _, song -> song.cid }) { index, song ->
                    SongRow(
                        song = song,
                        index = index,
                        isActive = state.currentSong?.cid == song.cid,
                        onClick = { onSelectSong(index) },
                    )
                    HairlineDivider()
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
