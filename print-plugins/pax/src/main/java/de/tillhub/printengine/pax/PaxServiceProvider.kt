package de.tillhub.printengine.pax

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.internal.InternalPrinterServiceProvider

object PaxServiceProvider : InternalPrinterServiceProvider {
    override fun build(context: Context): PrintService = PaxPrintService(context)
}
