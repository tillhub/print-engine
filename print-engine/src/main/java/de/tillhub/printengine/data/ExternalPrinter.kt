package de.tillhub.printengine.data

import java.util.Objects

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
     * - "STAR": Star Micronics printers
     * - "EPSON": Epson printers
     */
    val manufacturer: String
) {
    override fun toString(): String = "ExternalPrinter(" +
            "info=$info," +
            "connectionType=$connectionType," +
            "connectionAddress='$connectionAddress'," +
            "manufacturer=$manufacturer" +
            ")"

    override fun equals(other: Any?): Boolean = other is ExternalPrinter && info == other.info &&
            connectionType == other.connectionType &&
            connectionAddress == other.connectionAddress &&
            manufacturer == other.manufacturer

    override fun hashCode(): Int = Objects.hash(
        info,
        connectionType,
        connectionAddress,
        manufacturer
    )
}