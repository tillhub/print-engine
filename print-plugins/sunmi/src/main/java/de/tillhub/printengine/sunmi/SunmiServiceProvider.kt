package de.tillhub.printengine.sunmi

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterServiceProvider
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.data.ExternalPrinter

object SunmiServiceProvider : PrinterServiceProvider {
    override fun build(
        context: Context,
        printer: ExternalPrinter?,
        barcode: BarcodeEncoder?,
    ): PrintService = SunmiPrintService(context)
}
