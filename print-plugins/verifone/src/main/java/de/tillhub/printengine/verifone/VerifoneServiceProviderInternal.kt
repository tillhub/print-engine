package de.tillhub.printengine.verifone

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.internal.InternalPrinterServiceProvider

object VerifoneServiceProviderInternal : InternalPrinterServiceProvider {
    override fun build(context: Context): PrintService = VerifonePrintService(context)
}

