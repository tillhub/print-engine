package de.tillhub.printengine.sample

import android.content.Context
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterManufacturer
import de.tillhub.printengine.epson.EpsonServiceProvider
import de.tillhub.printengine.pax.PaxServiceProviderInternal
import de.tillhub.printengine.star.StarServiceProvider
import de.tillhub.printengine.sunmi.SunmiServiceProviderInternal
import de.tillhub.printengine.verifone.VerifoneServiceProviderInternal

object PrinterServiceFactory {

    fun createPrinterService(
        context: Context,
        externalPrinter: ExternalPrinter? = null
    ): PrintService {
        return when (PrinterManufacturer.get()) {
            PrinterManufacturer.PAX -> PaxServiceProviderInternal.build(context)
            PrinterManufacturer.SUNMI -> SunmiServiceProviderInternal.build(context)
            PrinterManufacturer.VERIFONE -> VerifoneServiceProviderInternal.build(context)
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