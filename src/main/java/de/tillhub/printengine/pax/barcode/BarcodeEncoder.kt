package de.tillhub.printengine.pax.barcode

import android.graphics.Bitmap

interface BarcodeEncoder {
    fun encodeAsBitmap(content: String, type: BarcodeType, imgWidth: Int, imgHeight: Int): Bitmap?
    fun formatCode(content: String, space: Int): String
}