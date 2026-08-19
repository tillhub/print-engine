package de.tillhub.printengine.data

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain

@RobolectricTest
internal class ImageBitmapExtTest :
    FunSpec({
        test("normalizeForPrintHead downscales an oversized image to the head width") {
            val image = gradientImage(width = 800, height = 400)

            val normalized = image.normalizeForPrintHead(HEAD_WIDTH_PX)

            normalized.width shouldBe HEAD_WIDTH_PX
            normalized.height shouldBe 192
        }

        test("normalizeForPrintHead does not upscale an image that already fits") {
            val image = gradientImage(width = 120, height = 60)

            val normalized = image.normalizeForPrintHead(HEAD_WIDTH_PX)

            normalized.width shouldBe 120
            normalized.height shouldBe 60
        }

        test("normalizeForPrintHead reduces the image to one bit monochrome") {
            val image = gradientImage(width = 200, height = 100)

            val pixels = IntArray(200 * 100)
            image.normalizeForPrintHead(HEAD_WIDTH_PX).readPixels(pixels)

            pixels.distinct().sorted() shouldBe listOf(BLACK, WHITE).sorted()
        }

        test("normalizeForPrintHead leaves a degenerate image alone") {
            val image = gradientImage(width = 4, height = 4)

            image.normalizeForPrintHead(headWidthPx = 0) shouldBe image
        }

        test("normalizeForPrintHead shrinks the encoded payload") {
            // The point of the whole exercise: a full colour photo is what overflows the IPC
            // transaction, and normalising it is what brings the receipt back under the limit.
            // Photographic detail is what PNG cannot compress away, so that is what is measured
            // here - a flat logo would understate the difference.
            val image = photoLikeImage(width = 800, height = 400)

            val normalizedSize = image.normalizeForPrintHead(HEAD_WIDTH_PX).encodeToBase64().length

            normalizedSize shouldBeLessThan image.encodeToBase64().length
        }

        test("encodeToBase64 does not wrap lines") {
            // Line breaks are ~2.7% of wasted payload inside a data URI, and iOS never emitted
            // them - so neither should Android.
            val image = gradientImage(width = 64, height = 64)

            image.encodeToBase64() shouldNotContain "\n"
        }
    })

/** A horizontal grey ramp: enough tone that dithering has something to do. */
private fun gradientImage(
    width: Int,
    height: Int,
): ImageBitmap = imageOf(width, height) { index ->
    val value = (index % width) * 255 / (width - 1)
    OPAQUE_ALPHA or (value shl 16) or (value shl 8) or value
}

/**
 * Deterministic per-pixel colour noise, standing in for a photo: like a photograph and unlike a
 * gradient, there is no redundancy for PNG to squeeze out.
 */
private fun photoLikeImage(
    width: Int,
    height: Int,
): ImageBitmap {
    var state = 1
    return imageOf(width, height) {
        state = state * LCG_MULTIPLIER + LCG_INCREMENT
        OPAQUE_ALPHA or (state ushr 8 and 0xFFFFFF)
    }
}

private fun imageOf(
    width: Int,
    height: Int,
    pixel: (index: Int) -> Int,
): ImageBitmap {
    val pixels = IntArray(width * height) { pixel(it) }
    return Bitmap
        .createBitmap(width, height, Bitmap.Config.ARGB_8888)
        .apply { setPixels(pixels, 0, width, 0, 0, width, height) }
        .asImageBitmap()
}

private const val HEAD_WIDTH_PX = 384
private const val LCG_MULTIPLIER = 1664525
private const val LCG_INCREMENT = 1013904223
private const val OPAQUE_ALPHA = 0xFF shl 24
private const val BLACK = 0xFF000000.toInt()
private const val WHITE = 0xFFFFFFFF.toInt()
