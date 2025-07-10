package de.tillhub.printengine

import de.tillhub.printengine.analytics.PrintAnalytics
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.barcode.BarcodeEncoderImpl
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.external.ExternalPrinterManager
import de.tillhub.printengine.external.ExternalPrinterManagerImpl
import de.tillhub.printengine.external.PrinterDiscovery
import de.tillhub.printengine.helper.SingletonHolder
import kotlinx.coroutines.flow.Flow

class PrintEngine private constructor() {
    private val externalPrinterManager: ExternalPrinterManager by lazy {
        ExternalPrinterManagerImpl()
    }

    private var printAnalytics: PrintAnalytics? = null

    fun setAnalytics(printAnalytics: PrintAnalytics): PrintEngine {
        this.printAnalytics = printAnalytics
        return this
    }

    val printer: Printer by lazy { PrinterContainer() }

    val barcodeEncoder: BarcodeEncoder by lazy { BarcodeEncoderImpl() }

    fun discoverExternalPrinters(vararg discoveries: PrinterDiscovery): Flow<DiscoveryState> =
        externalPrinterManager.discoverExternalPrinters(*discoveries)

    fun initPrinter(factory: (BarcodeEncoder) -> PrintService): Printer {
        (printer as PrinterContainer).initPrinter(
            PrinterImpl(factory(barcodeEncoder), printAnalytics)
        )
        return printer
    }


    companion object : SingletonHolder<PrintEngine>(::PrintEngine)
}
