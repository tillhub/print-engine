package de.tillhub.printengine.external

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.data.ExternalPrinter

interface ExternalPrinterManufacturer {
    fun build(context: Context, printer: ExternalPrinter): PrintService
}