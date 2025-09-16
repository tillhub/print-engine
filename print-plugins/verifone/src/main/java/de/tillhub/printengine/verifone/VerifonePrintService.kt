package de.tillhub.printengine.verifone

import android.content.Context
import com.verifone.peripherals.DirectPrintManager
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.data.PrinterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class VerifonePrintService(
    context: Context,
    barcodeEncoder: BarcodeEncoder,
) : PrintService() {
    private val connectionState = MutableStateFlow<PrinterState>(PrinterState.CheckingForPrinter)
    override val printerState: Flow<PrinterState> = connectionState

    private val connectionListener =
        object : DirectPrintManager.DirectPrintServiceListener {
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
        checkNotNull(DirectPrintManager.getInstance(context, false, connectionListener)) {
            "Error occurred, DirectPrintManager is null"
        }
    }

    override var printController: PrinterController? =
        VerifonePrintController(
            printManager = printManager,
            printerState = connectionState,
            barcodeEncoder = barcodeEncoder,
        )

    companion object {
        private const val DEFAULT_PAPER_WIDTH = 30
    }
}
