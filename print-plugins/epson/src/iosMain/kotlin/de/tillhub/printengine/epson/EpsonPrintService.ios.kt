package de.tillhub.printengine.epson

import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.PrinterState
import kotlinx.coroutines.flow.Flow

internal actual class EpsonPrintService : PrintService() {
    actual override var printController: PrinterController?
        get() = TODO("Not yet implemented")
        set(value) {}

    actual override val printerState: Flow<PrinterState>
        get() = TODO("Not yet implemented")
}
