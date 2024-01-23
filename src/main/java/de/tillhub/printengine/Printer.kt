package de.tillhub.printengine

import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrinterResult
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterSettings
import de.tillhub.printengine.data.PrintingPaperSpec
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents one printer and allows to manage the printers connection issue print commands.
 */
interface Printer {
    val settings: PrinterSettings
    fun observePrinterState(): StateFlow<PrinterState>
    suspend fun getPrinterInfo(): PrinterResult<PrinterInfo>
    suspend fun startPrintJob(job: PrintJob): PrinterResult<Unit>

    companion object {
        val DEFAULT_PRINTER_WIDTH = PrintingPaperSpec.PAX_PAPER_56MM.characterCount
    }
}
