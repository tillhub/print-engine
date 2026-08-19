package de.tillhub.printengine.data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.ImageInfo
import platform.posix.memcpy
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.jetbrains.skia.Bitmap as SkiaBitmap
import org.jetbrains.skia.Image as SkiaImage

/**
 * Wraps ARGB [pixels] in a Skia bitmap.
 *
 * On little-endian (all Apple platforms) the raw bytes of an ARGB int are already in BGRA
 * order, which is what Skia's N32 native format expects - so the pixels can be memcpy'd
 * across without any per-channel shuffling.
 */
@OptIn(ExperimentalForeignApi::class)
private fun skiaBitmapOf(
    pixels: IntArray,
    width: Int,
    height: Int,
): SkiaBitmap {
    val bytes = ByteArray(pixels.size * BYTES_PER_PIXEL)
    pixels.usePinned { src ->
        bytes.usePinned { dst ->
            memcpy(dst.addressOf(0), src.addressOf(0), bytes.size.toULong())
        }
    }

    val imageInfo = ImageInfo.makeN32Premul(width, height)
    return SkiaBitmap().apply {
        allocPixels(imageInfo)
        installPixels(imageInfo, bytes, width * BYTES_PER_PIXEL)
    }
}

/**
 * Encodes a Compose [ImageBitmap] to PNG bytes via Skia.
 * Handles pixel extraction, Skia bitmap/image lifecycle, and cleanup.
 * Returns null if PNG encoding fails.
 */
fun ImageBitmap.encodeToPngBytes(): ByteArray? {
    val buffer = IntArray(width * height)
    readPixels(buffer)

    val skiaBitmap = skiaBitmapOf(buffer, width, height)
    return try {
        val skiaImage = SkiaImage.makeFromBitmap(skiaBitmap)
        try {
            skiaImage.encodeToData(EncodedImageFormat.PNG)?.bytes
        } finally {
            skiaImage.close()
        }
    } finally {
        skiaBitmap.close()
    }
}

@OptIn(ExperimentalEncodingApi::class)
actual fun ImageBitmap.encodeToBase64(): String {
    val pngBytes = encodeToPngBytes()
        ?: error("Failed to encode image to PNG")
    return Base64.encode(pngBytes)
}

internal actual fun argbToImageBitmap(
    pixels: IntArray,
    width: Int,
    height: Int,
): ImageBitmap {
    val skiaBitmap = skiaBitmapOf(pixels, width, height)
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

private const val BYTES_PER_PIXEL = 4
