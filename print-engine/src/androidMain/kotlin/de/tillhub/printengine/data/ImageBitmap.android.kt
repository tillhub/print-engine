package de.tillhub.printengine.data

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

actual fun ImageBitmap.encodeToBase64(): String = ByteArrayOutputStream().let { stream ->
    this.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
    val b = stream.toByteArray()
    Base64.encodeToString(b, Base64.DEFAULT)
}

private const val PNG_QUALITY = 100
