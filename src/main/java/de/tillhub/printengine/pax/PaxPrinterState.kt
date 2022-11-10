package de.tillhub.printengine.pax

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
        fun fromCode(code: Int): PaxPrinterState = values()
            .firstOrNull { it.code == code } ?: Unknown
    }
}