package de.tillhub.printengine.epson

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterServiceProvider
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.data.ExternalPrinter

object EpsonServiceProvider : PrinterServiceProvider {
    override fun build(
        context: Context,
        printer: ExternalPrinter?,
        barcode: BarcodeEncoder?
    ): PrintService {
        requireNotNull(printer) {
            "EpsonServiceProvider requires an ExternalPrinter configuration"
        }
        return EpsonPrintService(context, printer)
    }
}