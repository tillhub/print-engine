package de.tillhub.printengine.data

import de.tillhub.printengine.external.ExternalPrinterManufacturer

class ExternalPrinter(
    val info: PrinterInfo,
    /**
     * The type of interface used to connect to the printer.
     * Must be one of: "Lan", "Usb", "Bluetooth"
     * - "Lan": Network connection
     * - "Usb": USB connection
     * - "Bluetooth": Bluetooth wireless connection
     */
    val connectionType: ConnectionType,
    /**
     * The address used to connect to the printer via its interface.
     *
     * Examples:
     * - LAN: `192.168.192.168`
     * - USB: `000000000000000000`
     * - Bluetooth: `00:22:15:7D:70:9C`
     */
    val connectionAddress: String,
    /**
     * The manufacturer of the printer.
     * Must be one of: "Star", "Epson"
     * - "Star": Star Micronics printers
     * - "Epson": Epson printers
     */
    val manufacturer: ExternalPrinterManufacturer
)