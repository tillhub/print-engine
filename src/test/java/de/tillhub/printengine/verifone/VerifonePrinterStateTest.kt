package de.tillhub.printengine.verifone

import com.verifone.peripherals.Printer
import de.tillhub.printengine.data.PrinterState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class VerifonePrinterStateTest : FunSpec({

    test("convert") {
     VerifonePrinterState.convert(Printer.STATUS_OUT_OF_PAPER) shouldBe PrinterState.Error.OutOfPaper
     VerifonePrinterState.convert(Printer.STATUS_OVER_TEMPERATURE) shouldBe PrinterState.Error.Overheated
     VerifonePrinterState.convert(Printer.STATUS_PAPER_JAM) shouldBe PrinterState.Error.PaperJam
     VerifonePrinterState.convert(Printer.STATUS_VOLTAGE_ERROR) shouldBe PrinterState.Error.VoltageTooLow
     VerifonePrinterState.convert(Printer.STATUS_LOW_BATTERY) shouldBe PrinterState.Error.VoltageTooLow
     VerifonePrinterState.convert(Printer.STATUS_FUSE) shouldBe PrinterState.Error.Malfunctions
     VerifonePrinterState.convert(Printer.STATUS_INTERNAL_ERROR) shouldBe PrinterState.Error.InternalError
     VerifonePrinterState.convert(Printer.STATUS_ERROR) shouldBe PrinterState.Error.InternalError
     VerifonePrinterState.convert(1234567890) shouldBe PrinterState.Error.Unknown
    }
})
