package com.example.musicsiren.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * 复刻官网排版签名：展示字体 + 宽松 letter-spacing。
 * Geometos/SourceHanSerifCN 为专有 web 字体，这里用系统替代（CJK 由 Noto 回退）。
 */
object SirenType {
    /** 展示衬线（SourceHanSerifCN Heavy 替代）：专辑名 / 大标题 */
    val DisplaySerif = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Black,
        fontSize = 22.sp,
        letterSpacing = 0.08.em,
    )

    /** 展示无衬线（Geometos 替代）：页标题 / 小节标题 */
    val DisplaySans = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.14.em,
    )

    /** 正文（SourceHanSansCN 替代） */
    val Body = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        letterSpacing = 0.02.em,
    )

    /** 标签 / 弱信息 */
    val Label = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        letterSpacing = 0.2.em,
    )

    /** 时间读数 / 序号（Bender 替代）：等宽 + 宽字距 */
    val Clock = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        letterSpacing = 0.25.em,
    )
}
