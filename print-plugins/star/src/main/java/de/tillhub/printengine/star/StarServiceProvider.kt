package de.tillhub.printengine.star

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.external.ExternalPrinterServiceProvider

object StarServiceProvider : ExternalPrinterServiceProvider {
    override fun build(context: Context, externalPrinter: ExternalPrinter): PrintService {
        requireNotNull(externalPrinter) { "StarServiceProvider requires an ExternalPrinter configuration" }
        return StarPrintService(context, externalPrinter)
    }
}
