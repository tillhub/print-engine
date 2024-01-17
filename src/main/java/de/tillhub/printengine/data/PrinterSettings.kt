package de.tillhub.printengine.data

data class PrinterSettings(
    var enabled: Boolean = true,
    var printingIntensity: PrintingIntensity = PrintingIntensity.DEFAULT
)