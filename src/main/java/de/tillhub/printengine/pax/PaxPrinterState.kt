package de.tillhub.printengine.pax

import de.tillhub.printengine.data.PrinterState

enum class PaxPrinterState(val code: Int) {
    Unknown(-1),
    Success(0),
    Busy(1),
    OutOfPaper(2),
    FormatPrintDataPacketError(3),
    Malfunctions(4),
    Overheated(8),
    VoltageTooLow(9),
    PrintingUnfinished(240),
    NotInstalledFontLibrary(252),
    DataPackageTooLong(254);

    companion object {
        fun fromCode(code: Int): PaxPrinterState =
            values().firstOrNull { it.code == code } ?: Unknown
        fun convert(state: PaxPrinterState): PrinterState =
            when (state) {
                Unknown -> PrinterState.Error.Unknown
                Success -> PrinterState.Connected
                Busy -> PrinterState.Busy
                OutOfPaper -> PrinterState.Error.OutOfPaper
                FormatPrintDataPacketError -> PrinterState.Error.FormatPrintDataPacketError
                Malfunctions -> PrinterState.Error.Malfunctions
                Overheated -> PrinterState.Error.Overheated
                VoltageTooLow -> PrinterState.Error.VoltageTooLow
                PrintingUnfinished -> PrinterState.Error.PrintingUnfinished
                NotInstalledFontLibrary -> PrinterState.Error.NotInstalledFontLibrary
                DataPackageTooLong -> PrinterState.Error.DataPackageTooLong
            }
    }
}