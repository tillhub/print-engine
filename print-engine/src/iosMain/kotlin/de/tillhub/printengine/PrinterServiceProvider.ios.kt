package de.tillhub.printengine

import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.data.ExternalPrinter

actual interface PrinterServiceProvider {
    fun build(
        printer: ExternalPrinter? = null,
        barcode: BarcodeEncoder? = null,
    ): PrintService
}
