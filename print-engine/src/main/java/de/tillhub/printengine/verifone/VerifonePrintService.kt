package de.tillhub.printengine.verifone

import android.content.Context
import com.verifone.peripherals.DirectPrintManager
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.barcode.BarcodeEncoderImpl
import de.tillhub.printengine.data.PrinterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class VerifonePrintService(context: Context) : PrintService() {

    private val connectionState = MutableStateFlow<PrinterState>(PrinterState.CheckingForPrinter)
    override val printerState: StateFlow<PrinterState> = connectionState

    private val connectionListener = object : DirectPrintManager.DirectPrintServiceListener {
        override fun onPrintServiceReady() {
            connectionState.value = PrinterState.Connected
            printManager.defaultPrinter.paperWidth = DEFAULT_PAPER_WIDTH
        }

        override fun onPrintServiceDisconnected() {
            connectionState.value = PrinterState.Error.ConnectionLost
        }

        override fun onPrintServiceDied() {
            connectionState.value = PrinterState.Error.NotAvailable
        }
    }

    private val printManager: DirectPrintManager by lazy {
        DirectPrintManager.getInstance(context, false, connectionListener)
            ?: throw IllegalStateException("Error occurred, DirectPrintManager is null")
    }

    override var printController: PrinterController? = VerifonePrintController(
        printManager = printManager,
        printerState = connectionState,
        barcodeEncoder = BarcodeEncoderImpl()
    )

    companion object {
        private const val DEFAULT_PAPER_WIDTH = 30
    }
}
