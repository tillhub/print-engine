package de.tillhub.printengine.epson

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.external.ExternalPrinterServiceProvider

object EpsonServiceProvider : ExternalPrinterServiceProvider {
    override fun build(context: Context, externalPrinter: ExternalPrinter): PrintService {
        requireNotNull(externalPrinter) { "EpsonServiceProvider requires an ExternalPrinter configuration" }
        return EpsonPrintService(context, externalPrinter)
    }
}