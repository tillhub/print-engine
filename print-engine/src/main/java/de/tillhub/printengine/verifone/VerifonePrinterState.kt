package de.tillhub.printengine.verifone

import com.verifone.peripherals.Printer
import de.tillhub.printengine.data.PrinterState

object VerifonePrinterState {
    fun convert(status: Int): PrinterState =
        when (status) {
            Printer.STATUS_OUT_OF_PAPER -> PrinterState.Error.OutOfPaper
            Printer.STATUS_OVER_TEMPERATURE -> PrinterState.Error.Overheated
            Printer.STATUS_PAPER_JAM -> PrinterState.Error.PaperJam
            Printer.STATUS_VOLTAGE_ERROR,
            Printer.STATUS_LOW_BATTERY -> PrinterState.Error.VoltageTooLow
            Printer.STATUS_FUSE -> PrinterState.Error.Malfunctions
            Printer.STATUS_INTERNAL_ERROR,
            Printer.STATUS_ERROR -> PrinterState.Error.Verifone.InternalError
            else -> PrinterState.Error.Unknown
        }
}