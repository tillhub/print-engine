package de.tillhub.printengine

import android.content.Context
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.data.ExternalPrinter

actual interface PrinterServiceProvider {
    fun build(
        context: Context,
        printer: ExternalPrinter? = null,
        barcode: BarcodeEncoder? = null,
    ): PrintService
}
