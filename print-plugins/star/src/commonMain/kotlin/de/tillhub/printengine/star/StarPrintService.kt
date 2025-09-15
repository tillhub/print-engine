package de.tillhub.printengine.star

import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.PrinterState
import kotlinx.coroutines.flow.Flow

expect class StarPrintService : PrintService {
    override var printController: PrinterController?
    override val printerState: Flow<PrinterState>
}