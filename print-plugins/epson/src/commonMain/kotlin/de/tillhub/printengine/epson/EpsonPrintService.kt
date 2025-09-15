package de.tillhub.printengine.epson

import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.PrinterState
import kotlinx.coroutines.flow.Flow

internal expect class EpsonPrintService : PrintService {
    override var printController: PrinterController?
    override val printerState: Flow<PrinterState>
}