package de.tillhub.printengine.verifone

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterServiceProvider
import de.tillhub.printengine.data.ExternalPrinter

object VerifoneServiceProvider : PrinterServiceProvider {
    override fun build(context: Context, externalPrinter: ExternalPrinter?): PrintService =
        VerifonePrintService(context)
}

