package de.tillhub.printengine.barcode

import de.tillhub.printengine.data.ImageBitmap

interface BarcodeEncoder {
    fun encodeAsBitmap(
        content: String,
        type: BarcodeType,
        imgWidth: Int,
        imgHeight: Int,
    ): ImageBitmap?
}
