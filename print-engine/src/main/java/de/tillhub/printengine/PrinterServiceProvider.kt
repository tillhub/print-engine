package de.tillhub.printengine

import android.content.Context
import de.tillhub.printengine.data.ExternalPrinter

interface PrinterServiceProvider {
    fun build(context: Context, externalPrinter: ExternalPrinter? = null): PrintService
}
