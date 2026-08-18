package de.tillhub.printengine.barcode

import androidx.compose.ui.graphics.ImageBitmap

internal expect class BarcodeEncoderImpl() : BarcodeEncoder {
    override fun encodeAsBitmap(
        content: String,
        type: BarcodeType,
        imgWidth: Int,
        imgHeight: Int,
    ): ImageBitmap?
}
