package de.tillhub.printengine.data

import androidx.compose.ui.graphics.ImageBitmap
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
 * Encodes a Compose [ImageBitmap] to PNG bytes via Skia.
 * Handles pixel extraction, Skia bitmap/image lifecycle, and cleanup.
 * Returns null if PNG encoding fails.
 */
@OptIn(ExperimentalForeignApi::class)
fun ImageBitmap.encodeToPngBytes(): ByteArray? {
    val w = width
    val h = height
    val buffer = IntArray(w * h)
    readPixels(buffer)

    // On little-endian (all Apple platforms), raw int bytes are already in
    // BGRA order which matches Skia's N32 native format — memcpy directly.
    val bytes = ByteArray(buffer.size * 4)
    buffer.usePinned { src ->
        bytes.usePinned { dst ->
            memcpy(dst.addressOf(0), src.addressOf(0), bytes.size.toULong())
        }
    }

    val imageInfo = ImageInfo.makeN32Premul(w, h)
    val skiaBitmap = SkiaBitmap().apply {
        allocPixels(imageInfo)
        installPixels(imageInfo, bytes, w * 4)
    }

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
