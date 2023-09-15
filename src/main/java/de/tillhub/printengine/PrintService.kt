package de.tillhub.printengine

import android.content.Context
import android.os.RemoteException
import de.tillhub.printengine.data.PrinterConnectionState
import de.tillhub.printengine.data.PrinterResult
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

abstract class PrintService {
    abstract var printController: PrinterController?
    abstract val printerConnectionState: StateFlow<PrinterConnectionState>
    abstract fun initPrinterService(context: Context)

    inline fun <T> withPrinterOrDefault(default: T, body: (PrinterController) -> T): T {
        return printController?.let {
            try {
                body(it)
            } catch (e: RemoteException) {
                Timber.e(e)
                default
            }
        } ?: default
    }

    inline fun <T> withPrinterCatching(body: (PrinterController) -> T): PrinterResult<T> {
        return printController?.let {
            try {
                PrinterResult.Success(body(it))
            } catch (e: RemoteException) {
                PrinterResult.Error.WithException(e)
            }
        } ?: PrinterResult.Error.PrinterNotConnected
    }
}