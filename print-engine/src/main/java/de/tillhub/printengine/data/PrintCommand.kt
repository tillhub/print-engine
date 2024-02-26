package de.tillhub.printengine.data

import android.graphics.Bitmap

sealed class PrintCommand {
    data class Text(val text: String) : PrintCommand()
    data class Image(val image: Bitmap) : PrintCommand()
    data class Barcode(val barcode: String) : PrintCommand()
    data class QrCode(val code: String) : PrintCommand()
    data class RawData(val data: RawPrinterData) : PrintCommand()
    /**
     *  Due to the distance between the paper hatch and the print head,
     *  the paper needs to be fed out automatically
     *  But if the Api does not support it, it will be replaced by printing three lines
     */
    data object FeedPaper : PrintCommand()
    /**
     *  Printer cuts paper and throws exception on machines without a cutter
     */
    data object CutPaper : PrintCommand()
}