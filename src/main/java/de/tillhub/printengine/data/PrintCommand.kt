package de.tillhub.printengine.data

import android.graphics.Bitmap

sealed class PrintCommand {
    data class Text(val text: String) : PrintCommand()
    data class Image(val image: Bitmap) : PrintCommand()
    data class Barcode(val barcode: String) : PrintCommand()
    data class QrCode(val code: String) : PrintCommand()
    data class RawData(val data: RawPrinterData) : PrintCommand()
    object FeedPaper : PrintCommand()
    object CutPaper : PrintCommand()
}