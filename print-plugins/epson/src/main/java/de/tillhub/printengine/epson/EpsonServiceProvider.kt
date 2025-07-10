package de.tillhub.printengine.epson

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterServiceProvider
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.external.ExternalPrinterServiceProvider

object EpsonServiceProvider : PrinterServiceProvider {
    override fun build(context: Context, printer: ExternalPrinter?): PrintService {
        requireNotNull(printer) {
            "EpsonServiceProvider requires an ExternalPrinter configuration"
        }
        return EpsonPrintService(context, printer)
    }
}