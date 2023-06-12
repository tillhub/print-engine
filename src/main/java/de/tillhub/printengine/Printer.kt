package de.tillhub.printengine

import android.content.Context
import android.graphics.Bitmap
import de.tillhub.printengine.data.*
import kotlinx.coroutines.flow.Flow

/**
 * Represents one printer and allows to manage the printers connection issue print commands.
 */
interface Printer {
    fun connect(context: Context)
    fun enable()
    fun disable()
    fun observeConnection(): Flow<PrinterConnectionState>
    fun getPrinterState(): PrinterState
    fun setPrintingIntensity(intensity: PrintingIntensity)
    suspend fun getPrinterInfo(): PrinterResult<PrinterInfo>
    suspend fun printText(text: String): PrinterResult<Unit>
    suspend fun printReceipt(text: String, headerImage: Bitmap?): PrinterResult<Unit>
    suspend fun printReceipt(
        rawReceiptText: String,
        barcode: String,
        headerImage: Bitmap?,
        footerImage: Bitmap?,
        signatureQr: String?
    ): PrinterResult<Unit>
    suspend fun printReceipt(receipt: RawReceipt): PrinterResult<Unit>

    /**
     *  Due to the distance between the paper hatch and the print head,
     *  the paper needs to be fed out automatically
     *  But if the Api does not support it, it will be replaced by printing three lines
     */
    suspend fun feedPaper(): PrinterResult<Unit>

    /**
     *  Printer cuts paper and throws exception on machines without a cutter
     */
    suspend fun cutPaper(): PrinterResult<Unit>

    companion object {
        val DEFAULT_PRINTER_WIDTH = PrintingPaperSpec.PAX_PAPER_56MM.characterCount
    }
}