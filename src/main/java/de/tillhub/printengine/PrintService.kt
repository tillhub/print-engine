package de.tillhub.printengine

import de.tillhub.printengine.data.PrinterConnectionState
import de.tillhub.printengine.data.PrinterResult
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

abstract class PrintService {
    abstract var printController: PrinterController?
    abstract val printerConnectionState: StateFlow<PrinterConnectionState>

    @Suppress("TooGenericExceptionCaught")
    inline fun <T> withPrinterOrDefault(default: T, body: (PrinterController) -> T): T {
        return printController?.let {
            try {
                body(it)
            } catch (e: Exception) {
                Timber.e(e)
                default
            }
        } ?: default
    }

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