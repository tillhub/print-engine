package de.tillhub.printengine.pax

import de.tillhub.printengine.data.PrinterState

internal enum class PaxPrinterState(val code: Int) {
    Unknown(code = -1),
    Success(code = 0),
    Busy(code = 1),
    OutOfPaper(code = 2),
    FormatPrintDataPacketError(code = 3),
    Malfunctions(code = 4),
    Overheated(code = 8),
    VoltageTooLow(code = 9),
    PrintingUnfinished(code = 240),
    NotInstalledFontLibrary(code = 252),
    DataPackageTooLong(code = 254);

    companion object {
        fun fromCode(code: Int): PaxPrinterState =
            entries.firstOrNull { it.code == code } ?: Unknown
        fun convert(state: PaxPrinterState): PrinterState =
            when (state) {
                Unknown -> PrinterState.Error.Unknown
                Success -> PrinterState.Connected
                Busy -> PrinterState.Busy
                OutOfPaper -> PrinterState.Error.OutOfPaper
                FormatPrintDataPacketError -> PrinterState.Error.Pax.FormatPrintDataPacketError
                Malfunctions -> PrinterState.Error.Malfunctions
                Overheated -> PrinterState.Error.Overheated
                VoltageTooLow -> PrinterState.Error.VoltageTooLow
                PrintingUnfinished -> PrinterState.Error.PrintingUnfinished
                NotInstalledFontLibrary -> PrinterState.Error.Pax.NotInstalledFontLibrary
                DataPackageTooLong -> PrinterState.Error.Pax.DataPackageTooLong
            }
    }
}
