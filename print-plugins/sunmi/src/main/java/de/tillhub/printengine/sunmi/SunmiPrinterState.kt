package de.tillhub.printengine.sunmi

import de.tillhub.printengine.data.PrinterState

/**
 * State of the printer when connected as defined by Sunmi.
 */
internal enum class SunmiPrinterState(val code: Int) {
    Unknown(code = -1),
    Connected(code = 1),
    Preparing(code = 2),
    AbnormalCommunication(code = 3),
    OutOfPaper(code = 4),
    Overheated(code = 5),
    CoverNotClosed(code = 6),
    PaperCutterAbnormal(code = 7),
    PaperCutterRecovered(code = 8),
    BlackMarkNotFound(code = 9),
    NotDetected(code = 505),
    FirmwareUpgradeFailed(code = 507);

    companion object {
        fun fromCode(code: Int): SunmiPrinterState =
            entries.firstOrNull { it.code == code } ?: Unknown
        fun convert(state: SunmiPrinterState): PrinterState =
            when (state) {
                Unknown -> PrinterState.Error.Unknown
                Connected -> PrinterState.Connected
                Preparing -> PrinterState.Preparing
                AbnormalCommunication -> PrinterState.Error.AbnormalCommunication
                OutOfPaper -> PrinterState.Error.OutOfPaper
                Overheated -> PrinterState.Error.Overheated
                CoverNotClosed -> PrinterState.Error.CoverNotClosed
                PaperCutterAbnormal -> PrinterState.Error.PaperCutterAbnormal
                PaperCutterRecovered -> PrinterState.Connected
                BlackMarkNotFound -> PrinterState.Error.BlackMarkNotFound
                NotDetected -> PrinterState.Error.NotAvailable
                FirmwareUpgradeFailed -> PrinterState.Error.FirmwareUpgradeFailed
            }
    }
}
