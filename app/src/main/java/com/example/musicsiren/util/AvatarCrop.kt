package com.example.musicsiren.util

import android.graphics.Bitmap

/** 屏幕正方形选区映射到原图的像素矩形（纯数据，便于单测）。 */
data class CropRect(val left: Int, val top: Int, val width: Int, val height: Int)

/**
 * 头像裁剪纯函数。
 * 模型：原图 W×H，正方形选区边长 squarePx；图片按 contain 适配出基准尺寸后整体缩放 [scale]，
 * 再相对选区中心平移 (offsetX, offsetY)（与裁剪页 graphicsLayer 一致）。
 */
object AvatarCrop {

    /** 使图片最窄边贴住正方形选区所需的最小缩放（保证覆盖、无空边）。 */
    fun minCoverScale(bitmapW: Int, bitmapH: Int, squarePx: Int): Float {
        require(bitmapW > 0 && bitmapH > 0 && squarePx > 0)
        val fit = minOf(squarePx.toFloat() / bitmapW, squarePx.toFloat() / bitmapH)
        return maxOf(squarePx / (bitmapW * fit), squarePx / (bitmapH * fit))
    }

    /** 把屏幕正方形选区映射回原图子矩形。 */
    fun computeCropRect(
        bitmapW: Int,
        bitmapH: Int,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        squarePx: Int,
    ): CropRect {
        require(bitmapW > 0 && bitmapH > 0 && squarePx > 0)
        val c = squarePx.toFloat()
        val fit = minOf(c / bitmapW, c / bitmapH)
        val displayW = bitmapW * fit * scale
        val displayH = bitmapH * fit * scale
        val left = (c - displayW) / 2f + offsetX
        val top = (c - displayH) / 2f + offsetY

        fun clamp01(v: Float) = v.coerceIn(0f, 1f)
        val x0 = (clamp01((0f - left) / displayW) * bitmapW).toInt()
        val y0 = (clamp01((0f - top) / displayH) * bitmapH).toInt()
        val x1 = (clamp01((c - left) / displayW) * bitmapW).toInt()
        val y1 = (clamp01((c - top) / displayH) * bitmapH).toInt()
        return CropRect(x0, y0, (x1 - x0).coerceAtLeast(1), (y1 - y0).coerceAtLeast(1))
    }

    /** 从原图裁剪 [rect] 并缩放为 target×target 正方形位图。 */
    fun cropToSquare(bitmap: Bitmap, rect: CropRect, target: Int = 256): Bitmap {
        val bw = bitmap.width
        val bh = bitmap.height
        val x = rect.left.coerceIn(0, bw - 1)
        val y = rect.top.coerceIn(0, bh - 1)
        val w = rect.width.coerceIn(1, bw - x)
        val h = rect.height.coerceIn(1, bh - y)
        val cropped = Bitmap.createBitmap(bitmap, x, y, w, h)
        return Bitmap.createScaledBitmap(cropped, target, target, true)
    }
}
