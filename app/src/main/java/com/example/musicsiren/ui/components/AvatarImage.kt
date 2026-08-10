package com.example.musicsiren.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.SirenType

/** 圆形头像：有 url 用 Coil 加载，否则用昵称首字 / ♪ 占位。 */
@Composable
fun AvatarImage(
    url: String?,
    nickname: String?,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier.size(size).clip(CircleShape).background(AccentCyan.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            CoverImage(url, Modifier.size(size).clip(CircleShape), contentScale = ContentScale.Crop)
        } else {
            Text(
                text = nickname?.firstOrNull()?.uppercase() ?: "♪",
                style = SirenType.DisplaySerif,
                color = AccentCyan,
            )
        }
    }
}
