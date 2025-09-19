package de.tillhub.printengine.data

import java.util.Objects

class PrinterSettings(
    var enabled: Boolean = true,
    var printingIntensity: PrintingIntensity = PrintingIntensity.DEFAULT
) {
    override fun toString() = "PrinterSettings(" +
            "enabled=$enabled, " +
            "printingIntensity=$printingIntensity" +
            ")"

    override fun equals(other: Any?) = other is PrinterSettings &&
            enabled == other.enabled &&
            printingIntensity == other.printingIntensity

    override fun hashCode() = Objects.hash(enabled, printingIntensity)
}
