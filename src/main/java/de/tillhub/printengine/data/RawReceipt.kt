package de.tillhub.printengine.data

/**
 * A receipt which can be printed by a printer.
 *
 * This receipt does not contain any human readable data. Instead this receipt defines all printer instructions
 * based on bytes.
 */
data class RawReceipt(
    val rawData: RawPrinterData
)