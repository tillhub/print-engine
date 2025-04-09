package de.tillhub.printengine.epson

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.external.ExternalPrinterManufacturer

object EpsonManufacturer : ExternalPrinterManufacturer {
    override fun build(context: Context, printer: ExternalPrinter): PrintService =
        EpsonPrintService(context, printer)
}