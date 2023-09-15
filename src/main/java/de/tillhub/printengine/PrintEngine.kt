package de.tillhub.printengine

import de.tillhub.printengine.analytics.PrintAnalytics
import de.tillhub.printengine.data.PrinterManufacturer
import de.tillhub.printengine.di.SingletonHolder
import de.tillhub.printengine.emulated.EmulatedPrinter
import de.tillhub.printengine.pax.PaxPrinter
import de.tillhub.printengine.pax.barcode.BarcodeEncoder
import de.tillhub.printengine.pax.barcode.BarcodeEncoderImpl
import de.tillhub.printengine.sunmi.SunmiPrinter

class PrintEngine private constructor(analytics: PrintAnalytics) {

    val printer: Printer by lazy {
        when (PrinterManufacturer.get()) {
            PrinterManufacturer.PAX -> PaxPrinter(analytics = analytics)
            PrinterManufacturer.SUNMI -> SunmiPrinter(analytics = analytics)
            PrinterManufacturer.UNKNOWN -> EmulatedPrinter()
        }
    }

    val barcodeEncoder: BarcodeEncoder by lazy {
        BarcodeEncoderImpl()
    }

    companion object : SingletonHolder<PrintEngine, PrintAnalytics, >(::PrintEngine)
}