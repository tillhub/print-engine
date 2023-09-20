package de.tillhub.printengine

import android.content.Context
import de.tillhub.printengine.analytics.PrintAnalytics
import de.tillhub.printengine.data.PrinterManufacturer
import de.tillhub.printengine.helper.SingletonHolder
import de.tillhub.printengine.emulated.EmulatedPrinter
import de.tillhub.printengine.pax.PaxPrintService
import de.tillhub.printengine.pax.PaxPrinter
import de.tillhub.printengine.pax.barcode.BarcodeEncoder
import de.tillhub.printengine.pax.barcode.BarcodeEncoderImpl
import de.tillhub.printengine.sunmi.SunmiPrintService
import de.tillhub.printengine.sunmi.SunmiPrinter

class PrintEngine private constructor(context: Context) {

    private var printAnalytics: PrintAnalytics? = null
    fun setAnalytics(printAnalytics: PrintAnalytics): PrintEngine {
        this.printAnalytics = printAnalytics
        return this
    }

    val printer: Printer by lazy {
        when (PrinterManufacturer.get()) {
            PrinterManufacturer.PAX -> PaxPrinter(PaxPrintService(context), printAnalytics)
            PrinterManufacturer.SUNMI -> SunmiPrinter(SunmiPrintService(context), printAnalytics)
            PrinterManufacturer.UNKNOWN -> EmulatedPrinter()
        }
    }

    val barcodeEncoder: BarcodeEncoder by lazy {
        BarcodeEncoderImpl()
    }

    companion object : SingletonHolder<PrintEngine, Context>(::PrintEngine)
}