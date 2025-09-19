package de.tillhub.printengine.pax

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Messenger
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.data.PrinterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Print service for encapsulating connection handling, error handling and convenience methods for working with
 * [PaxPrinterController].
 */
internal class PaxPrintService(context: Context, barcodeEncoder: BarcodeEncoder) : PrintService() {

    override var printController: PrinterController? = null

    private val connectionState = MutableStateFlow<PrinterState>(PrinterState.CheckingForPrinter)
    override val printerState: Flow<PrinterState> = connectionState

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            printController = PaxPrinterController(
                printService = DirectPrintServiceImpl(
                    requestMessenger = Messenger(service)
                ),
                printerState = connectionState,
                barcodeEncoder = barcodeEncoder
            )
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connectionState.value = PrinterState.Error.NotAvailable
        }
    }

    init {
        val intent = Intent().apply {
            component = ComponentName(PRINTING_PACKAGE, PRINTING_CLASS)
        }
        if (!context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            context.unbindService(connection)
            connectionState.value = PrinterState.Error.NotAvailable
        }
    }

    companion object {
        private const val PRINTING_PACKAGE = "de.ccv.payment.printservice"
        private const val PRINTING_CLASS = "de.ccv.payment.printservice.DirectPrintService"
    }
}
