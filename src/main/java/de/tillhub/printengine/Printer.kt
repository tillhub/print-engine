package de.tillhub.printengine

import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterConnectionState
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.PrinterResult
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrintingPaperSpec
import kotlinx.coroutines.flow.Flow

/**
 * Represents one printer and allows to manage the printers connection issue print commands.
 */
interface Printer {
    fun setEnabled(enabled: Boolean)
    fun observeConnection(): Flow<PrinterConnectionState>
    fun getPrinterState(): PrinterState
    fun setPrintingIntensity(intensity: PrintingIntensity)
    suspend fun getPrinterInfo(): PrinterResult<PrinterInfo>

    suspend fun startPrintJob(job: PrintJob): PrinterResult<Unit>

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