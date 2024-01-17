package de.tillhub.printengine.sunmi

import de.tillhub.printengine.data.PrinterState

/**
 * State of the printer when connected as defined by Sunmi.
 */
enum class SunmiPrinterState(val code: Int) {
    Unknown(-1),
    Connected(1),
    Preparing(2),
    AbnormalCommunication(3),
    OutOfPaper(4),
    Overheated(5),
    CoverNotClosed(6),
    PaperCutterAbnormal(7),
    PaperCutterRecovered(8),
    BlackMarkNotFound(9),
    NotDetected(505),
    FirmwareUpgradeFailed(507);

    companion object {
        fun fromCode(code: Int): SunmiPrinterState =
            values().firstOrNull { it.code == code } ?: Unknown
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