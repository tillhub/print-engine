package de.tillhub.printengine.barcode

import android.graphics.Bitmap

interface BarcodeEncoder {
    fun encodeAsBitmap(content: String, type: BarcodeType, imgWidth: Int, imgHeight: Int): Bitmap?
}
