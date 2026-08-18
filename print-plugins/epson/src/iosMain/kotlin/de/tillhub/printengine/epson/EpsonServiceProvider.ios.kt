package de.tillhub.printengine.epson

import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterServiceProvider
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.data.ExternalPrinter

actual object EpsonServiceProvider : PrinterServiceProvider {
    override fun build(
        printer: ExternalPrinter?,
        barcode: BarcodeEncoder?,
    ): PrintService {
        requireNotNull(printer) {
            "EpsonServiceProvider requires an ExternalPrinter configuration"
        }
        return EpsonPrintService(printer)
    }
}
