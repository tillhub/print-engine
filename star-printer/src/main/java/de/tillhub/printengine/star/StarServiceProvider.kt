package de.tillhub.printengine.star

import android.content.Context
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.external.PrinterServiceProvider

object StarServiceProvider : PrinterServiceProvider {
    override fun build(context: Context, printer: ExternalPrinter) =
        StarPrintService(context, printer)
}
