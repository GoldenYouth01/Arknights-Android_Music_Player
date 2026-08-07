package com.example.musicsiren.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * material-icons-core 里缺失的图标（暂停/上一首/下一首/下载/队列/下载完成），
 * 用 Material Design 标准路径数据自绘，避免引入庞大的 material-icons-extended。
 */
private fun sirenIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
        .addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black))
        .build()

val AppPause: ImageVector by lazy {
    sirenIcon("Pause", "M6,19h4V5H6v14zm8,-14v14h4V5h-4z")
}
val AppSkipNext: ImageVector by lazy {
    sirenIcon("SkipNext", "M6,18l8.5,-6L6,6v12zM16,6v12h2V6h-2z")
}
val AppSkipPrevious: ImageVector by lazy {
    // 精修版：竖条稍加宽加高，三角更饱满、指向左（跳回上一首）
    sirenIcon("SkipPrevious", "M6,5.5h2.5v13H6zM10,12l8.5,-6v12z")
}
val AppDownload: ImageVector by lazy {
    sirenIcon("Download", "M19,9h-4V3H9v6H5l7,7 7,-7zM5,18v2h14v-2H5z")
}
val AppQueueMusic: ImageVector by lazy {
    sirenIcon("QueueMusic", "M15,6H3v2h12V6zm0,4H3v2h12v-2zM3,16h8v-2H3v2zM17,6v8.18c-0.31,-0.11 -0.65,-0.18 -1,-0.18 -1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3V8h3V6h-5z")
}
val AppDownloadDone: ImageVector by lazy {
    sirenIcon("DownloadDone", "M5,18h14v2H5v-2zm4.6,-2.7L5,10.7l2,-1.1 2.6,2.6L17,6l2,1.4 -9.4,9.9z")
}
val AppShuffle: ImageVector by lazy {
    sirenIcon("Shuffle", "M10.59,9.17L5.41,4 4,5.41l5.17,5.17 1.42,-1.41zM14.5,4l2.04,2.04L4,18.59 5.41,20 17.96,7.46 20,9.5V4h-5.5zm0.33,9.41l-1.41,1.41 3.13,3.13L14.5,20H20v-5.5l-2.04,2.04 -3.13,-3.13z")
}
val AppRepeat: ImageVector by lazy {
    sirenIcon("Repeat", "M7,7h10v3l4,-4 -4,-4v3H5v6h2V7zM17,17H7v-3l-4,4 4,4v-3h12v-6h-2v4z")
}
val AppRepeatOne: ImageVector by lazy {
    sirenIcon("RepeatOne", "M7,7h10v3l4,-4 -4,-4v3H5v6h2V7zM17,17H7v-3l-4,4 4,4v-3h12v-6h-2v4zM13,15V9h-1l-2,1v1h1.5v4H13z")
}
