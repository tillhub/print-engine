package de.tillhub.printengine.di

import de.tillhub.printengine.Printer
import de.tillhub.printengine.analytics.PrintAnalytics
import de.tillhub.printengine.data.PrinterManufacturer
import de.tillhub.printengine.emulated.EmulatedPrinter
import de.tillhub.printengine.pax.PaxPrinter
import de.tillhub.printengine.pax.barcode.BarcodeEncoder
import de.tillhub.printengine.pax.barcode.BarcodeEncoderImpl
import de.tillhub.printengine.sunmi.SunmiPrinter

object PrinterInjectionProvider {

    fun providePrinter(analytics: PrintAnalytics): Printer {
        return when (PrinterManufacturer.get()) {
            PrinterManufacturer.PAX -> PaxPrinter(analytics = analytics)
            PrinterManufacturer.SUNMI -> SunmiPrinter(analytics = analytics)
            PrinterManufacturer.UNKNOWN -> EmulatedPrinter()
        }
    }

    fun provideBarcodeEncoder(): BarcodeEncoder {
        return BarcodeEncoderImpl()
    }
}