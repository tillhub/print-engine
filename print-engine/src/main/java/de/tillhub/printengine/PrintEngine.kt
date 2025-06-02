package de.tillhub.printengine

import android.content.Context
import de.tillhub.printengine.analytics.PrintAnalytics
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.barcode.BarcodeEncoderImpl
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.PrinterManufacturer
import de.tillhub.printengine.external.ExternalPrinterContainer
import de.tillhub.printengine.external.ExternalPrinterManager
import de.tillhub.printengine.external.ExternalPrinterManagerImpl
import de.tillhub.printengine.external.PrinterDiscovery
import de.tillhub.printengine.helper.SingletonHolder
import de.tillhub.printengine.pax.PaxPrintService
import de.tillhub.printengine.sunmi.SunmiPrintService
import de.tillhub.printengine.verifone.VerifonePrintService
import kotlinx.coroutines.flow.Flow

class PrintEngine private constructor(context: Context) {

    private val externalPrinterManager: ExternalPrinterManager by lazy {
        ExternalPrinterManagerImpl()
    }

    private var printAnalytics: PrintAnalytics? = null
    fun setAnalytics(printAnalytics: PrintAnalytics): PrintEngine {
        this.printAnalytics = printAnalytics
        return this
    }

    val printer: Printer by lazy {
        when (PrinterManufacturer.get()) {
            PrinterManufacturer.PAX -> PrinterImpl(PaxPrintService(context), printAnalytics)
            PrinterManufacturer.SUNMI -> PrinterImpl(SunmiPrintService(context), printAnalytics)
            PrinterManufacturer.VERIFONE -> PrinterImpl(VerifonePrintService(context), printAnalytics)
            PrinterManufacturer.UNKNOWN -> ExternalPrinterContainer()
        }
    }

    val barcodeEncoder: BarcodeEncoder by lazy { BarcodeEncoderImpl() }

    fun discoverExternalPrinters(vararg discoveries: PrinterDiscovery): Flow<DiscoveryState> =
        externalPrinterManager.discoverExternalPrinters(*discoveries)

    fun initPrinter(printService: PrintService): Result<Unit> {
        return if (printer is ExternalPrinterContainer) {
            (printer as ExternalPrinterContainer).initPrinter(
                PrinterImpl(printService, printAnalytics)
            )
            Result.success(Unit)
        } else {
            Result.failure(
                IllegalStateException("Cannot initialize printer, it is not an external printer.")
            )
        }
    }

    companion object : SingletonHolder<PrintEngine, Context>(::PrintEngine)
}
