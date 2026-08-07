package com.example.musicsiren.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.example.musicsiren.ui.theme.AccentCyan

/**
 * 复刻官网「正在播放」的青蓝辉光：
 * `drop-shadow(0 0 10px rgba(160,237,255,.5))` 用 Compose shadow 近似。
 */
fun Modifier.sirenGlow(active: Boolean): Modifier {
    if (!active) return this
    return this
        .clip(RoundedCornerShape(4.dp))
        .shadow(
            elevation = 10.dp,
            shape = RoundedCornerShape(4.dp),
            ambientColor = AccentCyan.copy(alpha = 0.5f),
            spotColor = AccentCyan.copy(alpha = 0.5f),
            clip = false,
        )
}
