package de.tillhub.printengine.data

import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.ImageInfo
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.jetbrains.skia.Bitmap as SkiaBitmap
import org.jetbrains.skia.Image as SkiaImage

@OptIn(ExperimentalEncodingApi::class)
actual fun ImageBitmap.encodeToBase64(): String {
    val width = this.width
    val height = this.height
    val buffer = IntArray(width * height)
    this.readPixels(buffer)

    // readPixels returns ARGB_8888 int format. On little-endian systems,
    // storing these ints as bytes produces BGRA byte order, which matches
    // Skia's N32 native format (BGRA_8888 on little-endian).
    val bytes = ByteArray(buffer.size * 4)
    for (i in buffer.indices) {
        val pixel = buffer[i]
        bytes[i * 4 + 0] = (pixel and 0xFF).toByte()
        bytes[i * 4 + 1] = (pixel shr 8 and 0xFF).toByte()
        bytes[i * 4 + 2] = (pixel shr 16 and 0xFF).toByte()
        bytes[i * 4 + 3] = (pixel shr 24 and 0xFF).toByte()
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
