package de.tillhub.printengine.pax

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterServiceProvider
import de.tillhub.printengine.data.ExternalPrinter

object PaxServiceProvider : PrinterServiceProvider {
    override fun build(context: Context, externalPrinter: ExternalPrinter?): PrintService =
        PaxPrintService(context)
}
