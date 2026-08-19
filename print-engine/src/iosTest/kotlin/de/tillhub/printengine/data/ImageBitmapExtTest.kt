package de.tillhub.printengine.data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.ImageInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jetbrains.skia.Bitmap as SkiaBitmap
import org.jetbrains.skia.Image as SkiaImage

/**
 * Covers the Skia backed half of [normalizeForPrintHead]: the shared algorithm is tested in
 * [MonochromeImageTest], this checks that reading pixels out of a Compose [ImageBitmap] and
 * building one back up works on iOS.
 */
internal class ImageBitmapExtTest {
    @Test
    fun `normalizeForPrintHead downscales an oversized image to the head width`() {
        val normalized = gradientImage(width = 800, height = 400)
            .normalizeForPrintHead(HEAD_WIDTH_PX)

        assertEquals(HEAD_WIDTH_PX, normalized.width)
        assertEquals(192, normalized.height)
    }

    @Test
    fun `normalizeForPrintHead does not upscale an image that already fits`() {
        val normalized = gradientImage(width = 120, height = 60)
            .normalizeForPrintHead(HEAD_WIDTH_PX)

        assertEquals(120, normalized.width)
        assertEquals(60, normalized.height)
    }

    @Test
    fun `normalizeForPrintHead reduces the image to one bit monochrome`() {
        val normalized = gradientImage(width = 200, height = 100)
            .normalizeForPrintHead(HEAD_WIDTH_PX)

        val pixels = IntArray(normalized.width * normalized.height)
        normalized.readPixels(pixels)

        assertTrue(
            pixels.all { it == BLACK || it == WHITE },
            "normalized output must be 1 bit monochrome",
        )
    }

    @Test
    fun `normalizeForPrintHead shrinks the encoded payload`() {
        // Photographic detail is what PNG cannot compress away, so that is what is measured
        // here - a flat logo or a smooth ramp would understate the difference.
        val image = photoLikeImage(width = 800, height = 400)

        val normalizedSize = image.normalizeForPrintHead(HEAD_WIDTH_PX).encodeToBase64().length

        assertTrue(
            normalizedSize < image.encodeToBase64().length,
            "normalising should shrink the payload",
        )
    }

    /** A horizontal grey ramp: enough tone that dithering has something to do. */
    private fun gradientImage(
        width: Int,
        height: Int,
    ): ImageBitmap = imageOf(width, height) { index ->
        val value = ((index % width) * 255 / (width - 1)).toByte()
        // Neutral grey, so all three colour channels carry the same value.
        byteArrayOf(value, value, value, OPAQUE_ALPHA)
    }

    /**
     * Deterministic per-pixel colour noise, standing in for a photo: like a photograph and
     * unlike a gradient, there is no redundancy for PNG to squeeze out.
     */
    private fun photoLikeImage(
        width: Int,
        height: Int,
    ): ImageBitmap {
        var state = 1
        return imageOf(width, height) {
            state = state * LCG_MULTIPLIER + LCG_INCREMENT
            byteArrayOf(
                (state ushr 8).toByte(),
                (state ushr 16).toByte(),
                (state ushr 24).toByte(),
                OPAQUE_ALPHA,
            )
        }
    }

    /** Builds an [ImageBitmap] straight through Skia, independent of production code. */
    private fun imageOf(
        width: Int,
        height: Int,
        // N32 on little-endian Apple platforms is BGRA.
        bgraPixel: (index: Int) -> ByteArray,
    ): ImageBitmap {
        val bytes = ByteArray(width * height * BYTES_PER_PIXEL)
        for (index in 0 until width * height) {
            bgraPixel(index).copyInto(bytes, index * BYTES_PER_PIXEL)
        }

        val imageInfo = ImageInfo.makeN32Premul(width, height)
        val skiaBitmap = SkiaBitmap().apply {
            allocPixels(imageInfo)
            installPixels(imageInfo, bytes, width * BYTES_PER_PIXEL)
        }
        return try {
            val skiaImage = SkiaImage.makeFromBitmap(skiaBitmap)
            try {
                skiaImage.toComposeImageBitmap()
            } finally {
                skiaImage.close()
            }
        } finally {
            skiaBitmap.close()
        }
    }

    private companion object {
        const val HEAD_WIDTH_PX = 384
        const val LCG_MULTIPLIER = 1664525
        const val LCG_INCREMENT = 1013904223
        const val BYTES_PER_PIXEL = 4
        const val OPAQUE_ALPHA = 0xFF.toByte()
        const val BLACK = 0xFF000000.toInt()
        const val WHITE = 0xFFFFFFFF.toInt()
    }
}
