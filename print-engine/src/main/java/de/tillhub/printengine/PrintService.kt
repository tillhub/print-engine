package de.tillhub.printengine

import de.tillhub.printengine.data.PrinterResult
import de.tillhub.printengine.data.PrinterState
import kotlinx.coroutines.flow.StateFlow

abstract class PrintService {
    abstract var printController: PrinterController?
    abstract val printerState: StateFlow<PrinterState>

    @Suppress("TooGenericExceptionCaught")
    inline fun <T> withPrinterCatching(body: (PrinterController) -> T): PrinterResult<T> {
        return printController?.let {
            try {
                PrinterResult.Success(body(it))
            } catch (e: Exception) {
                PrinterResult.Error.WithException(e)
            }
        } ?: PrinterResult.Error.PrinterNotConnected
    }
}
