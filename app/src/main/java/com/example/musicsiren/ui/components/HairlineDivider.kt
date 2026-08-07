package com.example.musicsiren.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.musicsiren.ui.theme.HairlineWhite

/** 1px 水平渐变分割线 —— 取代实线边框（官网签名细节）。 */
@Composable
fun HairlineDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        HairlineWhite.copy(alpha = 0f),
                        HairlineWhite,
                        HairlineWhite.copy(alpha = 0f),
                    )
                )
            )
    )
}
