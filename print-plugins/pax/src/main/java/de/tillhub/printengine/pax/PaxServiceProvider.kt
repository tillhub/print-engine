package de.tillhub.printengine.pax

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterServiceProvider
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.data.ExternalPrinter

object PaxServiceProvider : PrinterServiceProvider {
    override fun build(
        context: Context,
        printer: ExternalPrinter?,
        barcode: BarcodeEncoder?,
    ): PrintService {
        requireNotNull(barcode) {
            "PaxServiceProvider requires a BarcodeEncoder configuration"
        }
        return PaxPrintService(context, barcode)
    }
}
