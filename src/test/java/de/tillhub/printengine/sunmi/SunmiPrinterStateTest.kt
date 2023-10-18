package de.tillhub.printengine.sunmi

import de.tillhub.printengine.data.PrinterState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SunmiPrinterStateTest : FunSpec({

    test("code") {
     SunmiPrinterState.fromCode(-1) shouldBe SunmiPrinterState.Unknown
     SunmiPrinterState.fromCode(1) shouldBe SunmiPrinterState.Connected
     SunmiPrinterState.fromCode(2) shouldBe SunmiPrinterState.Preparing
     SunmiPrinterState.fromCode(3) shouldBe SunmiPrinterState.AbnormalCommunication
     SunmiPrinterState.fromCode(4) shouldBe SunmiPrinterState.OutOfPaper
     SunmiPrinterState.fromCode(5) shouldBe SunmiPrinterState.Overheated
     SunmiPrinterState.fromCode(6) shouldBe SunmiPrinterState.CoverNotClosed
     SunmiPrinterState.fromCode(7) shouldBe SunmiPrinterState.PaperCutterAbnormal
     SunmiPrinterState.fromCode(8) shouldBe SunmiPrinterState.PaperCutterRecovered
     SunmiPrinterState.fromCode(9) shouldBe SunmiPrinterState.BlackMarkNotFound
     SunmiPrinterState.fromCode(505) shouldBe SunmiPrinterState.NotDetected
     SunmiPrinterState.fromCode(507) shouldBe SunmiPrinterState.FirmwareUpgradeFailed
    }

    test("convert") {
     SunmiPrinterState.convert(SunmiPrinterState.Unknown) shouldBe PrinterState.Error.Unknown
     SunmiPrinterState.convert(SunmiPrinterState.Connected) shouldBe PrinterState.Connected
     SunmiPrinterState.convert(SunmiPrinterState.Preparing) shouldBe PrinterState.Preparing
     SunmiPrinterState.convert(SunmiPrinterState.AbnormalCommunication) shouldBe PrinterState.Error.AbnormalCommunication
     SunmiPrinterState.convert(SunmiPrinterState.OutOfPaper) shouldBe PrinterState.Error.OutOfPaper
     SunmiPrinterState.convert(SunmiPrinterState.Overheated) shouldBe PrinterState.Error.Overheated
     SunmiPrinterState.convert(SunmiPrinterState.CoverNotClosed) shouldBe PrinterState.Error.CoverNotClosed
     SunmiPrinterState.convert(SunmiPrinterState.PaperCutterAbnormal) shouldBe PrinterState.Error.PaperCutterAbnormal
     SunmiPrinterState.convert(SunmiPrinterState.PaperCutterRecovered) shouldBe PrinterState.Connected
     SunmiPrinterState.convert(SunmiPrinterState.BlackMarkNotFound) shouldBe PrinterState.Error.BlackMarkNotFound
     SunmiPrinterState.convert(SunmiPrinterState.NotDetected) shouldBe PrinterState.PrinterNotDetected
     SunmiPrinterState.convert(SunmiPrinterState.FirmwareUpgradeFailed) shouldBe PrinterState.Error.FirmwareUpgradeFailed
    }
})
