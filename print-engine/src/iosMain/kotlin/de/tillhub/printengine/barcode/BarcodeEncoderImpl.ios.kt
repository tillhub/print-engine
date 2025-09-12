package de.tillhub.printengine.barcode

import de.tillhub.printengine.data.ImageBitmap

internal actual class BarcodeEncoderImpl : BarcodeEncoder {
    actual override fun encodeAsBitmap(
        content: String,
        type: BarcodeType,
        imgWidth: Int,
        imgHeight: Int
    ): ImageBitmap? {
        TODO("Not yet implemented")
    }
}