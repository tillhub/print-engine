package de.tillhub.printengine.data

/**
 * The connection state of a [Printer].
 */
sealed class PrinterConnectionState {
    object PrinterNotAvailable : PrinterConnectionState()
    object CheckingForPrinter : PrinterConnectionState()
    object PrinterConnected : PrinterConnectionState()
    object PrinterConnectionLost : PrinterConnectionState()
}