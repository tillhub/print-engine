package de.tillhub.printengine.pax

import android.content.Context
import com.pax.dal.IDAL
import com.pax.dal.exceptions.PrinterDevException
import com.pax.neptunelite.api.NeptuneLiteUser
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.barcode.BarcodeEncoderImpl
import de.tillhub.printengine.data.PrinterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

/**
 * Print service for encapsulating connection handling, error handling and convenience methods for working with
 * [PaxPrinterController].
 */
internal class PaxPrintService(context: Context) : PrintService() {
    override var printController: PrinterController? = null

    private val connectionState = MutableStateFlow<PrinterState>(PrinterState.CheckingForPrinter)
    override val printerState: StateFlow<PrinterState> = connectionState

    init {
        @Suppress("TooGenericExceptionCaught")
        try {
            printController = PaxPrinterController(
                context = context,
                printerState = connectionState,
                barcodeEncoder = BarcodeEncoderImpl()
            )
            connectionState.value = PrinterState.Connected
        } catch (e: Exception) {
            Timber.e(e)
            connectionState.value = PrinterState.Error.NotAvailable
        }
    }
}
