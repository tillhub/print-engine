package de.tillhub.printengine.data

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream

actual fun ImageBitmap.encodeToBase64(): String = ByteArrayOutputStream().let { stream ->
    this.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
    val b = stream.toByteArray()
    Base64.encodeToString(b, Base64.NO_WRAP)
}

internal actual fun argbToImageBitmap(
    pixels: IntArray,
    width: Int,
    height: Int,
): ImageBitmap = createBitmap(width, height).apply {
    setPixels(pixels, 0, width, 0, 0, width, height)
}.asImageBitmap()

private const val PNG_QUALITY = 100
