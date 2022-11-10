package de.tillhub.printengine.pax

import android.content.Context
import com.pax.dal.IDAL
import com.pax.dal.exceptions.PrinterDevException
import com.pax.neptunelite.api.NeptuneLiteUser
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.PrinterConnectionState
import de.tillhub.printengine.pax.barcode.BarcodeEncoderImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Print service for encapsulating connection handling, error handling and convenience methods for working with
 * [PaxPrinterController].
 */
class PaxPrintService(
    private var dal: IDAL? = null,
    override var printController: PrinterController? = null
) : PrintService() {

    private val connectionState = MutableStateFlow<PrinterConnectionState>(PrinterConnectionState.CheckingForPrinter)
    override val printerConnectionState: StateFlow<PrinterConnectionState> = connectionState

    @Suppress("TooGenericExceptionCaught")
    override fun initPrinterService(context: Context) {
        if (printController == null) {
            try {
                printController = PaxPrinterController(getDal(context).printer, BarcodeEncoderImpl())
                connectionState.value = PrinterConnectionState.PrinterConnected
            } catch (e: PrinterDevException) { // Printer Initialization
                e.printStackTrace()
                connectionState.value = PrinterConnectionState.PrinterNotAvailable
            } catch (e: Exception) {
                e.printStackTrace()
                connectionState.value = PrinterConnectionState.PrinterNotAvailable
            }
        }
    }

    private fun getDal(context: Context): IDAL {
        if (dal == null) {
            dal = NeptuneLiteUser.getInstance().getDal(context)
        }
        return dal ?: throw IllegalStateException("Error occurred, DAL is null")
    }
}