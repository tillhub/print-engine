package de.tillhub.printengine.epson

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterServiceProvider
import de.tillhub.printengine.data.ExternalPrinter

object EpsonServiceProvider : PrinterServiceProvider {
    override fun build(context: Context, externalPrinter: ExternalPrinter?): PrintService {
        requireNotNull(externalPrinter) { "EpsonServiceProvider requires an ExternalPrinter configuration" }
        return EpsonPrintService(context, externalPrinter)
    }
}