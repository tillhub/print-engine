package de.tillhub.printengine.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Exercises [MonochromeImage.normalize], the entry point the print path uses. The scaling and
 * dithering steps are covered through it rather than individually, so the tests describe what a
 * caller can rely on instead of how the pixels get there.
 */
internal class MonochromeImageTest {
    @Test
    fun `downscales to the head width and preserves the aspect ratio`() {
        val normalized = MonochromeImage.normalize(
            pixels = gradient(width = 800, height = 400),
            sourceWidth = 800,
            sourceHeight = 400,
            headWidthPx = HEAD_WIDTH_PX,
        )

        assertEquals(HEAD_WIDTH_PX, normalized.width)
        assertEquals(192, normalized.height)
        assertEquals(HEAD_WIDTH_PX * 192, normalized.pixels.size)
    }

    @Test
    fun `does not upscale an image that already fits`() {
        val normalized = MonochromeImage.normalize(
            pixels = gradient(width = 120, height = 60),
            sourceWidth = 120,
            sourceHeight = 60,
            headWidthPx = HEAD_WIDTH_PX,
        )

        assertEquals(120, normalized.width)
        assertEquals(60, normalized.height)
    }

    @Test
    fun `never collapses a wide flat image to no height at all`() {
        val normalized = MonochromeImage.normalize(
            pixels = gradient(width = 1000, height = 2),
            sourceWidth = 1000,
            sourceHeight = 2,
            headWidthPx = 10,
        )

        assertEquals(10, normalized.width)
        assertEquals(1, normalized.height)
    }

    @Test
    fun `emits only opaque black or white pixels`() {
        val normalized = MonochromeImage.normalize(
            pixels = gradient(width = 200, height = 100),
            sourceWidth = 200,
            sourceHeight = 100,
            headWidthPx = HEAD_WIDTH_PX,
        )

        assertTrue(
            normalized.pixels.all { it == BLACK || it == WHITE },
            "normalised output must be 1 bit monochrome",
        )
    }

    @Test
    fun `prints a transparent pixel as unprinted paper`() {
        // Transparent is black in ARGB terms; on paper it has to come out white.
        val normalized = normalizeSinglePixel(TRANSPARENT)

        assertEquals(WHITE, normalized)
    }

    @Test
    fun `weights the colour channels by luminance`() {
        // Green carries most of the perceived brightness, blue almost none, so at full saturation
        // green lands on the white side of the threshold while red and blue land on the black side.
        assertEquals(WHITE, normalizeSinglePixel(GREEN))
        assertEquals(BLACK, normalizeSinglePixel(RED))
        assertEquals(BLACK, normalizeSinglePixel(BLUE))
    }

    @Test
    fun `dithers flat mid grey into a mix of black and white`() {
        val midGrey = IntArray(SIZE * SIZE) { MID_GREY }

        val normalized = MonochromeImage.normalize(
            pixels = midGrey,
            sourceWidth = SIZE,
            sourceHeight = SIZE,
            headWidthPx = HEAD_WIDTH_PX,
        )

        assertTrue(normalized.pixels.any { it == BLACK }, "expected some black dots")
        assertTrue(normalized.pixels.any { it == WHITE }, "expected some white dots")
    }

    @Test
    fun `averages the pixels it merges instead of sampling them`() {
        // One dark pixel among three white ones, halved in width. Sampling every other pixel would
        // read white twice and lose the dark one outright; averaging carries it into the merged
        // pixel, which then dithers to black.
        val normalized = MonochromeImage.normalize(
            pixels = intArrayOf(WHITE, BLACK, WHITE, WHITE),
            sourceWidth = 4,
            sourceHeight = 1,
            headWidthPx = 2,
        )

        assertEquals(listOf(BLACK, WHITE), normalized.pixels.toList())
    }

    private fun normalizeSinglePixel(pixel: Int): Int = MonochromeImage
        .normalize(
            pixels = intArrayOf(pixel),
            sourceWidth = 1,
            sourceHeight = 1,
            headWidthPx = HEAD_WIDTH_PX,
        ).pixels
        .single()

    /** A horizontal grey ramp, the shape that shows whether tone survives normalisation. */
    private fun gradient(
        width: Int,
        height: Int,
    ): IntArray = IntArray(width * height) { index ->
        val value = (index % width) * 255 / (width - 1)
        OPAQUE_ALPHA or (value shl 16) or (value shl 8) or value
    }

    private companion object {
        const val HEAD_WIDTH_PX = 384
        const val SIZE = 8
        const val OPAQUE_ALPHA = 0xFF shl 24
        const val BLACK = 0xFF000000.toInt()
        const val WHITE = 0xFFFFFFFF.toInt()
        const val MID_GREY = 0xFF7F7F7F.toInt()
        const val TRANSPARENT = 0x00000000
        const val RED = 0xFFFF0000.toInt()
        const val GREEN = 0xFF00FF00.toInt()
        const val BLUE = 0xFF0000FF.toInt()
    }
}
