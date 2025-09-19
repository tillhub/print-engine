package de.tillhub.printengine.star

import de.tillhub.printengine.data.PrinterState

internal object StarPrinterErrorCodes {
    const val UNKNOWN_CODE = -1
    const val DEVICE_HAS_ERROR_CODE = 1000
    const val PRINTER_HOLDING_PAPER_CODE = 1001
    const val PRINTING_TIMEOUT_CODE = 1002
    const val BLUETOOTH_UNAVAILABLE_CODE = 2000
    const val NETWORK_UNAVAILABLE_CODE = 2001
    const val USB_UNAVAILABLE_CODE = 2002
}

internal enum class StarPrinterErrorState(
    val code: Int,
) {
    Unknown(StarPrinterErrorCodes.UNKNOWN_CODE),
    DeviceHasError(StarPrinterErrorCodes.DEVICE_HAS_ERROR_CODE),
    PrinterHoldingPaper(StarPrinterErrorCodes.PRINTER_HOLDING_PAPER_CODE),
    PrintingTimeout(StarPrinterErrorCodes.PRINTING_TIMEOUT_CODE),
    BluetoothUnavailable(StarPrinterErrorCodes.BLUETOOTH_UNAVAILABLE_CODE),
    NetworkUnavailable(StarPrinterErrorCodes.NETWORK_UNAVAILABLE_CODE),
    UsbUnavailable(StarPrinterErrorCodes.USB_UNAVAILABLE_CODE),
    ;

    companion object {
        fun fromCode(code: Int): StarPrinterErrorState = entries.firstOrNull { it.code == code } ?: Unknown

        fun convert(state: StarPrinterErrorState): PrinterState = when (state) {
            Unknown -> PrinterState.Error.Unknown
            DeviceHasError -> PrinterState.Error.AbnormalCommunication
            PrinterHoldingPaper -> PrinterState.Error.PaperJam
            PrintingTimeout -> PrinterState.Error.PrintingUnfinished
            BluetoothUnavailable -> PrinterState.Error.NotAvailable
            NetworkUnavailable -> PrinterState.Error.NotAvailable
            UsbUnavailable -> PrinterState.Error.NotAvailable
        }
    }
}
