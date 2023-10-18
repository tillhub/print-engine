package de.tillhub.printengine.verifone

import android.content.Context
import com.verifone.peripherals.DirectPrintManager
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.PrinterConnectionState
import de.tillhub.printengine.barcode.BarcodeEncoderImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VerifonePrintService(context: Context) : PrintService() {

    private val connectionState = MutableStateFlow<PrinterConnectionState>(PrinterConnectionState.CheckingForPrinter)
    override val printerConnectionState: StateFlow<PrinterConnectionState> = connectionState

    private val connectionListener = object : DirectPrintManager.DirectPrintServiceListener {
        override fun onPrintServiceReady() {
            connectionState.value = PrinterConnectionState.PrinterConnected
            printManager.defaultPrinter.paperWidth = 30
        }

        override fun onPrintServiceDisconnected() {
            connectionState.value = PrinterConnectionState.PrinterConnectionLost
        }

        override fun onPrintServiceDied() {
            connectionState.value = PrinterConnectionState.PrinterNotAvailable
        }
    }

    private val printManager: DirectPrintManager by lazy {
        DirectPrintManager.getInstance(context, false, connectionListener)
            ?: throw IllegalStateException("Error occurred, DirectPrintManager is null")
    }

    override var printController: PrinterController? = VerifonePrintController(printManager, BarcodeEncoderImpl())
}
