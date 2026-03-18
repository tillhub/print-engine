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

@OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class)
actual fun ImageBitmap.encodeToBase64(): String {
    val width = this.width
    val height = this.height
    val buffer = IntArray(width * height)
    this.readPixels(buffer)

    // On little-endian (all Apple platforms), raw int bytes are already in
    // BGRA order which matches Skia's N32 native format — memcpy directly.
    val bytes = ByteArray(buffer.size * 4)
    buffer.usePinned { src ->
        bytes.usePinned { dst ->
            memcpy(dst.addressOf(0), src.addressOf(0), bytes.size.toULong())
        }
    }

    val imageInfo = ImageInfo.makeN32Premul(width, height)
    val skiaBitmap = SkiaBitmap().apply {
        allocPixels(imageInfo)
        installPixels(imageInfo, bytes, width * 4)
    }

    val skiaImage = SkiaImage.makeFromBitmap(skiaBitmap)
    val pngData = skiaImage.encodeToData(EncodedImageFormat.PNG)
        ?: error("Failed to encode image to PNG")

    return Base64.encode(pngData.bytes)
}
