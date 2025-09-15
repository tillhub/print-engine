package de.tillhub.printengine.star

import android.content.Context
import com.starmicronics.stario10.InterfaceType
import de.tillhub.printengine.data.ConnectionType
import com.starmicronics.stario10.PrinterDelegate
import com.starmicronics.stario10.StarConnectionSettings
import com.starmicronics.stario10.StarIO10Exception
import com.starmicronics.stario10.StarPrinter
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

actual class StarPrintService(context: Context, printer: ExternalPrinter) : PrintService() {

    private val connectionState = MutableStateFlow<PrinterState>(PrinterState.CheckingForPrinter)
    actual override val printerState: Flow<PrinterState> = connectionState


    private val connectionListener = object : PrinterDelegate() {
        override fun onCommunicationError(e: StarIO10Exception) {
            super.onCommunicationError(e)
            connectionState.value = PrinterState.Error.ConnectionLost
        }

        override fun onReady() {
            super.onReady()
            connectionState.value = PrinterState.Connected
        }

        override fun onPaperReady() {
            connectionState.value = PrinterState.Connected
        }

        override fun onError() {
            connectionState.value = PrinterState.Error.Malfunctions
        }

        override fun onPaperEmpty() {
            super.onPaperEmpty()
            connectionState.value = PrinterState.Error.OutOfPaper
        }

    }

    private val starPrinter: StarPrinter by lazy {
        val interfaceType = printer.connectionType.toStarConnectionType()
        val settings = StarConnectionSettings(interfaceType, printer.connectionAddress)
        StarPrinter(settings, context).apply {
            printerDelegate = connectionListener
        }
    }

    actual override var printController: PrinterController? = StarPrinterController(
        starPrinter,
        connectionState,
    )

    private fun ConnectionType.toStarConnectionType(): InterfaceType = when (this) {
        ConnectionType.LAN -> InterfaceType.Lan
        ConnectionType.BLUETOOTH -> InterfaceType.Bluetooth
        ConnectionType.USB -> InterfaceType.Usb
        else -> throw IllegalArgumentException("Unsupported connection type: $this")
    }
}
