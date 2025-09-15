package de.tillhub.printengine.star

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterServiceProvider
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.data.ExternalPrinter

actual object StarServiceProvider : PrinterServiceProvider {
    override fun build(
        context: Context,
        printer: ExternalPrinter?,
        barcode: BarcodeEncoder?
    ): PrintService {
        requireNotNull(printer) { "StarServiceProvider requires an ExternalPrinter configuration" }
        return StarPrintService(context, printer)
    }
}
