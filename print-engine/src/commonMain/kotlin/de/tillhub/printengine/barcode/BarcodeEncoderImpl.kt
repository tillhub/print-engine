package de.tillhub.printengine.barcode

import de.tillhub.printengine.data.ImageBitmap

internal expect class BarcodeEncoderImpl() : BarcodeEncoder {
    override fun encodeAsBitmap(
        content: String,
        type: BarcodeType,
        imgWidth: Int,
        imgHeight: Int
    ): ImageBitmap?
}