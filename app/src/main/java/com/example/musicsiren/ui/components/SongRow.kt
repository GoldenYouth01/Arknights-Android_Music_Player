package com.example.musicsiren.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.musicsiren.domain.model.Song
import com.example.musicsiren.ui.theme.PressTint
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.TextSecondary

/**
 * 歌曲行：Bender 风格编号 + 歌名 + 艺人。
 * 激活行：青蓝辉光 + 左侧青色竖条 + 按下底色；末尾可挂 [trailing]（如下载图标）。
 * [menuItems] 非空时右侧渲染 `⋮` 按钮 + 下拉菜单；菜单项点击后需调用传入的 dismiss 关闭菜单。
 */
@Composable
fun SongRow(
    song: Song,
    index: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    menuItems: (@Composable (dismiss: () -> Unit) -> Unit)? = null,
    coverUrl: String? = null,
) {
    val accent = MaterialTheme.colorScheme.primary
    var menuOpen by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .sirenGlow(isActive)
            .background(if (isActive) PressTint else Color.Transparent)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 激活竖条
            Box(
                Modifier
                    .width(2.dp)
                    .height(28.dp)
                    .background(if (isActive) accent else Color.Transparent)
            )
            Spacer(Modifier.width(12.dp))
            if (coverUrl != null) {
                CoverImage(
                    url = coverUrl,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(2.dp)),
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = if (index >= 0) (index + 1).toString().padStart(2, '0') else "··",
                style = SirenType.Clock,
                color = if (isActive) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(28.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = song.name,
                    style = SirenType.Body.copy(fontWeight = FontWeight.Medium),
                    color = if (isActive) accent else MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (song.artists.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = song.artists.joinToString(" / "),
                        style = SirenType.Label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
            if (menuItems != null) {
                Spacer(Modifier.width(4.dp))
                Box {
                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        menuItems({ menuOpen = false })
                    }
                }
            }
        }
        // 右侧边缘青色竖条（激活）
        if (isActive) {
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(accent)
            )
        }
    }
}
