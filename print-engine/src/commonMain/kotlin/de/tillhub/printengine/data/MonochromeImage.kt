package de.tillhub.printengine.data

/**
 * Pixel level preparation of images for a monochrome thermal print head.
 *
 * Deliberately free of any platform bitmap type: the whole algorithm is shared and unit
 * tested in commonTest, and only the final "pixels to platform bitmap" step needs an
 * expect/actual (see [argbToImageBitmap]).
 */
internal object MonochromeImage {
    /**
     * The result of [normalize]: opaque ARGB pixels that are already pure black or white,
     * together with the size they describe.
     */
    class Pixels(
        val pixels: IntArray,
        val width: Int,
        val height: Int,
    )

    /**
     * Downscales the [sourceWidth] x [sourceHeight] ARGB [pixels] so their width fits
     * [headWidthPx], then reduces them to pure black and white.
     *
     * Images at or below [headWidthPx] keep their size: a print head cannot render detail
     * beyond its own width, so upscaling would inflate the payload without adding anything
     * printable.
     */
    fun normalize(
        pixels: IntArray,
        sourceWidth: Int,
        sourceHeight: Int,
        headWidthPx: Int,
    ): Pixels {
        val targetWidth = targetWidth(sourceWidth, headWidthPx)
        val targetHeight = targetHeight(sourceWidth, sourceHeight, targetWidth)

        val gray = toGrayscale(pixels)
        val scaled = if (targetWidth == sourceWidth && targetHeight == sourceHeight) {
            gray
        } else {
            downscale(gray, sourceWidth, sourceHeight, targetWidth, targetHeight)
        }

        return Pixels(
            pixels = toOpaqueArgb(dither(scaled, targetWidth, targetHeight)),
            width = targetWidth,
            height = targetHeight,
        )
    }

    /** Never wider than the head, never upscaled. */
    private fun targetWidth(
        sourceWidth: Int,
        headWidthPx: Int,
    ): Int = minOf(sourceWidth, headWidthPx)

    /** Height that preserves the aspect ratio of [sourceWidth] x [sourceHeight] at [targetWidth]. */
    private fun targetHeight(
        sourceWidth: Int,
        sourceHeight: Int,
        targetWidth: Int,
    ): Int = when {
        targetWidth == sourceWidth -> sourceHeight
        else -> maxOf(1, (sourceHeight.toLong() * targetWidth / sourceWidth).toInt())
    }

    /**
     * Converts ARGB pixels to 0..255 luminance, compositing over white first so that
     * transparent regions come out as unprinted paper rather than as black.
     */
    private fun toGrayscale(pixels: IntArray): IntArray = IntArray(pixels.size) { index ->
        val pixel = pixels[index]
        val alpha = (pixel ushr ALPHA_SHIFT) and CHANNEL_MASK
        val red = (pixel ushr RED_SHIFT) and CHANNEL_MASK
        val green = (pixel ushr GREEN_SHIFT) and CHANNEL_MASK
        val blue = pixel and CHANNEL_MASK

        val luminance = (RED_WEIGHT * red + GREEN_WEIGHT * green + BLUE_WEIGHT * blue) shr WEIGHT_SHIFT
        when (alpha) {
            CHANNEL_MASK -> luminance
            else -> (luminance * alpha + CHANNEL_MASK * (CHANNEL_MASK - alpha)) / CHANNEL_MASK
        }
    }

    /**
     * Box filter downscale of a luminance plane: every target pixel is the average of the
     * source pixels it covers. Averaging rather than sampling matters here, because dropping
     * pixels from a barcode-like pattern loses strokes that averaging preserves as grey - and
     * grey is what [dither] turns back into detail.
     */
    @Suppress("NestedBlockDepth")
    private fun downscale(
        gray: IntArray,
        sourceWidth: Int,
        sourceHeight: Int,
        targetWidth: Int,
        targetHeight: Int,
    ): IntArray {
        val scaled = IntArray(targetWidth * targetHeight)
        for (targetY in 0 until targetHeight) {
            val startY = targetY * sourceHeight / targetHeight
            val endY = maxOf(startY + 1, (targetY + 1) * sourceHeight / targetHeight)
            for (targetX in 0 until targetWidth) {
                val startX = targetX * sourceWidth / targetWidth
                val endX = maxOf(startX + 1, (targetX + 1) * sourceWidth / targetWidth)

                var sum = 0
                var count = 0
                for (sourceY in startY until endY) {
                    val rowOffset = sourceY * sourceWidth
                    for (sourceX in startX until endX) {
                        sum += gray[rowOffset + sourceX]
                        count++
                    }
                }
                scaled[targetY * targetWidth + targetX] = sum / count
            }
        }
        return scaled
    }

    /**
     * Floyd-Steinberg error diffusion: each pixel is pushed to black or white and the
     * rounding error is spread over the neighbours not yet visited. That is what keeps
     * photographic gradients readable on a head that can only print "dot" or "no dot".
     *
     * [gray] is consumed as the error accumulator, so the caller must not reuse it.
     */
    private fun dither(
        gray: IntArray,
        width: Int,
        height: Int,
    ): IntArray {
        val output = IntArray(gray.size)
        for (y in 0 until height) {
            val rowOffset = y * width
            for (x in 0 until width) {
                val index = rowOffset + x
                val value = gray[index]
                val quantized = if (value < THRESHOLD) BLACK else WHITE
                output[index] = quantized

                val error = value - quantized
                if (error == 0) continue

                if (x + 1 < width) {
                    gray[index + 1] += error * RIGHT_WEIGHT / ERROR_DIVISOR
                }
                if (y + 1 < height) {
                    val belowOffset = index + width
                    if (x > 0) {
                        gray[belowOffset - 1] += error * BELOW_LEFT_WEIGHT / ERROR_DIVISOR
                    }
                    gray[belowOffset] += error * BELOW_WEIGHT / ERROR_DIVISOR
                    if (x + 1 < width) {
                        gray[belowOffset + 1] += error * BELOW_RIGHT_WEIGHT / ERROR_DIVISOR
                    }
                }
            }
        }
        return output
    }

    /** Expands a luminance plane back into opaque ARGB pixels. */
    private fun toOpaqueArgb(gray: IntArray): IntArray = IntArray(gray.size) { index ->
        val value = gray[index].coerceIn(BLACK, WHITE)
        OPAQUE_ALPHA or (value shl RED_SHIFT) or (value shl GREEN_SHIFT) or value
    }

    private const val ALPHA_SHIFT = 24
    private const val RED_SHIFT = 16
    private const val GREEN_SHIFT = 8
    private const val CHANNEL_MASK = 0xFF
    private const val OPAQUE_ALPHA = 0xFF shl ALPHA_SHIFT

    // Integer approximation of the Rec. 601 luma coefficients (0.299, 0.587, 0.114).
    private const val RED_WEIGHT = 77
    private const val GREEN_WEIGHT = 151
    private const val BLUE_WEIGHT = 28
    private const val WEIGHT_SHIFT = 8

    private const val BLACK = 0
    private const val WHITE = 255
    private const val THRESHOLD = 128

    // Floyd-Steinberg diffusion kernel.
    private const val RIGHT_WEIGHT = 7
    private const val BELOW_LEFT_WEIGHT = 3
    private const val BELOW_WEIGHT = 5
    private const val BELOW_RIGHT_WEIGHT = 1
    private const val ERROR_DIVISOR = 16
}
