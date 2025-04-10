package de.tillhub.printengine.epson

import com.epson.epos2.Epos2CallbackCode
import com.epson.epos2.Epos2Exception
import com.epson.epos2.printer.Printer
import com.epson.epos2.printer.PrinterStatusInfo
import de.tillhub.printengine.data.PrinterState

object EpsonPrinterErrorState {
    fun epsonExceptionToState(e: Epos2Exception) = when (e.errorStatus) {
        Epos2Exception.ERR_PARAM -> PrinterState.Error.Epson.DataError
        Epos2Exception.ERR_CONNECT -> PrinterState.Error.NotAvailable
        Epos2Exception.ERR_TIMEOUT,
        Epos2Exception.ERR_DISCONNECT -> PrinterState.Error.ConnectionLost
        Epos2Exception.ERR_MEMORY -> PrinterState.Error.Epson.MemoryError

        else -> PrinterState.Error.Epson.InternalError
    }

    fun epsonStatusToState(code: Int, status: PrinterStatusInfo) = when (code) {
        Epos2CallbackCode.CODE_SUCCESS -> PrinterState.Connected
        Epos2CallbackCode.CODE_PRINTING -> PrinterState.Busy
        else -> when {
            status.online == Printer.FALSE -> PrinterState.Error.ConnectionLost
            status.paper == Printer.PAPER_EMPTY -> PrinterState.Error.OutOfPaper
            status.coverOpen == Printer.TRUE -> PrinterState.Error.CoverNotClosed
            status.errorStatus == Printer.UNKNOWN ||
                    status.errorStatus == Printer.MECHANICAL_ERR -> {
                PrinterState.Error.Malfunctions
            }
            status.errorStatus == Printer.AUTOCUTTER_ERR -> {
                PrinterState.Error.PaperCutterAbnormal
            }
            status.errorStatus == Printer.UNRECOVER_ERR -> {
                when (status.unrecoverError) {
                    Printer.LOW_VOLTAGE_ERR -> PrinterState.Error.VoltageTooLow
                    else -> PrinterState.Error.Malfunctions
                }
            }
            status.errorStatus == Printer.AUTORECOVER_ERR -> when (status.autoRecoverError) {
                Printer.HEAD_OVERHEAT,
                Printer.MOTOR_OVERHEAT,
                Printer.BATTERY_OVERHEAT -> PrinterState.Error.Overheated
                Printer.WRONG_PAPER -> PrinterState.Error.PaperAbnormal
                Printer.COVER_OPEN -> PrinterState.Error.CoverNotClosed
                else -> PrinterState.Error.Malfunctions
            }
            status.batteryLevel == Printer.BATTERY_LEVEL_0 -> {
                PrinterState.Error.VoltageTooLow
            }

            else -> PrinterState.Connected
        }
    }
}