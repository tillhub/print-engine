package de.tillhub.printengine.star

import StarPrintService
import android.content.Context
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.external.ExternalPrinterManufacturer

object StarManufacturer : ExternalPrinterManufacturer {
    override fun build(context: Context, printer: ExternalPrinter) =
        StarPrintService(context, printer)
}
