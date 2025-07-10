package de.tillhub.printengine.verifone

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterServiceProvider
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.data.ExternalPrinter

object VerifoneServiceProvider : PrinterServiceProvider {
    override fun build(
        context: Context,
        printer: ExternalPrinter?,
        barcode: BarcodeEncoder?
    ): PrintService = VerifonePrintService(context)
}

