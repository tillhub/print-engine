package de.tillhub.printengine.star

import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterServiceProvider
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.data.ExternalPrinter

actual object StarServiceProvider : PrinterServiceProvider {
    override fun build(
        printer: ExternalPrinter?,
        barcode: BarcodeEncoder?,
    ): PrintService = StarPrintService()
}
