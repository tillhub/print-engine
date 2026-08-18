package de.tillhub.printengine.barcode

import androidx.compose.ui.graphics.ImageBitmap

interface BarcodeEncoder {
    fun encodeAsBitmap(
        content: String,
        type: BarcodeType,
        imgWidth: Int,
        imgHeight: Int,
    ): ImageBitmap?
}
