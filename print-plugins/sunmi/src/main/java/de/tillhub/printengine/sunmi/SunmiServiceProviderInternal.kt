package de.tillhub.printengine.sunmi

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.internal.InternalPrinterServiceProvider

object SunmiServiceProviderInternal : InternalPrinterServiceProvider {
    override fun build(context: Context): PrintService = SunmiPrintService(context)
}
