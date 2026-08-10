package com.example.musicsiren.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.Background
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.TextPrimary
import com.example.musicsiren.ui.theme.TextSecondary
import com.example.musicsiren.util.AvatarCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 头像裁剪页：正方形框选区域，双指缩放 / 拖动移动，确认后以正方形选区为准。
 * 图片经 contain 适配后整体缩放平移，选区即整个正方形容器。
 */
@Composable
fun AvatarCropScreen(
    uri: Uri,
    onConfirm: (Bitmap) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(null, uri) {
        value = withContext(Dispatchers.IO) { decodeSampledBitmap(context, uri) }
    }
    val bmp = bitmap // 委托属性无法 smart-cast，先读到局部

    BackHandler(onBack = onBack)

    Box(Modifier.fillMaxSize().background(Background)) {
        if (bmp == null) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("图片加载失败", style = SirenType.Body, color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onBack) { Text("返回") }
            }
            return@Box
        }

        var scale by remember(bmp) { mutableFloatStateOf(1f) }
        var offsetX by remember(bmp) { mutableFloatStateOf(0f) }
        var offsetY by remember(bmp) { mutableFloatStateOf(0f) }
        var squarePx by remember(bmp) { mutableIntStateOf(0) }

        // 正方形容器测量完成后，初始缩放为「恰好覆盖」，避免初始露出空边
        LaunchedEffect(squarePx) {
            if (squarePx > 0) {
                scale = AvatarCrop.minCoverScale(bmp.width, bmp.height, squarePx)
                offsetX = 0f
                offsetY = 0f
            }
        }

        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = TextPrimary)
                }
                Column(Modifier.weight(1f)) {
                    Text("裁剪头像", style = SirenType.DisplaySerif, color = TextPrimary)
                    Text("双指缩放 · 拖动移动", style = SirenType.Label, color = TextSecondary)
                }
            }

            Box(
                Modifier
                    .fillMaxWidth(0.92f)
                    .aspectRatio(1f)
                    .align(Alignment.CenterHorizontally)
                    .clipToBounds()
                    .onSizeChanged { squarePx = it.width }
                    .pointerInput(bmp, squarePx) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            if (squarePx <= 0) return@detectTransformGestures
                            val minS = AvatarCrop.minCoverScale(bmp.width, bmp.height, squarePx)
                            val newScale = (scale * zoom).coerceIn(minS, 5f)
                            scale = newScale
                            val fit = minOf(squarePx.toFloat() / bmp.width, squarePx.toFloat() / bmp.height)
                            val baseW = bmp.width * fit
                            val baseH = bmp.height * fit
                            val maxX = maxOf(0f, (baseW * newScale - squarePx) / 2f)
                            val maxY = maxOf(0f, (baseH * newScale - squarePx) / 2f)
                            offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                            offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                        }
                    }
                    .border(2.dp, AccentCyan),
                contentAlignment = Alignment.Center,
            ) {
                val density = LocalDensity.current
                val fit = if (squarePx > 0) {
                    minOf(squarePx.toFloat() / bmp.width, squarePx.toFloat() / bmp.height)
                } else {
                    1f
                }
                val baseW = bmp.width * fit
                val baseH = bmp.height * fit
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(with(density) { baseW.toDp() }, with(density) { baseH.toDp() })
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        },
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = onBack, modifier = Modifier.weight(1f)) { Text("取消") }
                Button(
                    onClick = {
                        if (squarePx > 0) {
                            val rect = AvatarCrop.computeCropRect(
                                bmp.width, bmp.height, scale, offsetX, offsetY, squarePx
                            )
                            onConfirm(AvatarCrop.cropToSquare(bmp, rect))
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("确认") }
            }
        }
    }
}

/** 采样解码（≤2048px 防 OOM）+ EXIF 旋转归一。 */
private fun decodeSampledBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val resolver = context.contentResolver
        // 先读边界计算采样率
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        val maxDim = 2048
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxDim) sample *= 2
        // 真实解码
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bmp = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) } ?: return null
        // EXIF 旋转
        val rotation = readExifRotation(context, uri)
        if (rotation != 0) {
            val m = Matrix().apply { postRotate(rotation.toFloat()) }
            val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
            if (rotated !== bmp) bmp.recycle()
            rotated
        } else {
            bmp
        }
    } catch (e: Exception) {
        null
    }
}

private fun readExifRotation(context: Context, uri: Uri): Int {
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val orientation = ExifInterface(input)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } ?: 0
    } catch (e: Exception) {
        0
    }
}
