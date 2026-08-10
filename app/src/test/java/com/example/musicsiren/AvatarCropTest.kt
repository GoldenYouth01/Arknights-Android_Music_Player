package com.example.musicsiren

import com.example.musicsiren.util.AvatarCrop
import com.example.musicsiren.util.CropRect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 头像裁剪纯函数测试：正方形选区 ↔ 原图子矩形映射。 */
class AvatarCropTest {

    @Test
    fun `minCoverScale makes 4x3 image cover square`() {
        // 4:3 图，正方形 1000：contain 基准宽 1000/高 750，需 scale≥1000/750≈1.333
        assertEquals(1.3333f, AvatarCrop.minCoverScale(4000, 3000, 1000), 0.001f)
    }

    @Test
    fun `cover scale no offset yields square crop`() {
        // cover scale、offset=0：可见区 = 图片正中 3000×3000 正方形
        val s = AvatarCrop.minCoverScale(4000, 3000, 1000)
        val rect = AvatarCrop.computeCropRect(4000, 3000, s, 0f, 0f, 1000)
        assertEquals(CropRect(500, 0, 3000, 3000), rect)
    }

    @Test
    fun `zoom in keeps crop centered and within bounds`() {
        val rect = AvatarCrop.computeCropRect(4000, 3000, 2f, 0f, 0f, 1000)
        assertEquals(CropRect(1000, 500, 2000, 2000), rect)
    }

    @Test
    fun `offset shifts crop region`() {
        val s = AvatarCrop.minCoverScale(4000, 3000, 1000)
        val rect = AvatarCrop.computeCropRect(4000, 3000, s, 200f, 150f, 1000)
        // 向右下平移：左/上边缘贴边（clamp），高精确，宽可能因浮点 ±1px
        assertEquals(0, rect.left)
        assertEquals(0, rect.top)
        assertEquals(2550, rect.height)
        assertTrue(rect.width in 2899..2901)
    }

    @Test
    fun `extreme offset clamps to image bounds`() {
        val rect = AvatarCrop.computeCropRect(4000, 3000, 1.333f, 100_000f, 100_000f, 1000)
        assertTrue(rect.left >= 0 && rect.top >= 0)
        assertTrue(rect.left + rect.width <= 4000)
        assertTrue(rect.top + rect.height <= 3000)
    }

    @Test
    fun `small bitmap crop within bounds`() {
        // 正方形图片：cover scale=1，crop 即整图
        val rect = AvatarCrop.computeCropRect(1000, 1000, 1f, 0f, 0f, 1000)
        assertEquals(CropRect(0, 0, 1000, 1000), rect)
    }
}
