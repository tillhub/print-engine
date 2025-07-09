package de.tillhub.printengine.sample

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterManufacturer
import de.tillhub.printengine.epson.EpsonServiceProvider
import de.tillhub.printengine.pax.PaxServiceProvider
import de.tillhub.printengine.star.StarServiceProvider
import de.tillhub.printengine.sunmi.SunmiServiceProvider
import de.tillhub.printengine.verifone.VerifoneServiceProvider

object PrinterServiceFactory {

    fun createPrinterService(
        context: Context,
        externalPrinter: ExternalPrinter? = null
    ): PrintService {
        return when (PrinterManufacturer.get()) {
            PrinterManufacturer.PAX -> PaxServiceProvider.build(context)
            PrinterManufacturer.SUNMI -> SunmiServiceProvider.build(context)
            PrinterManufacturer.VERIFONE -> VerifoneServiceProvider.build(context)
            PrinterManufacturer.UNKNOWN -> createExternalPrinterService(context, externalPrinter)
        }
    }

    private fun createExternalPrinterService(
        context: Context,
        externalPrinter: ExternalPrinter?
    ): PrintService {
        val printer = externalPrinter
            ?: throw IllegalArgumentException("External printer info required for UNKNOWN manufacturer")
        return when (printer.manufacturer.uppercase()) {
            "EPSON" -> EpsonServiceProvider.build(context, externalPrinter)
            "STAR" -> StarServiceProvider.build(context, externalPrinter)
            else -> throw IllegalArgumentException("Unsupported external printer: ${printer.manufacturer}")
        }
    }
}