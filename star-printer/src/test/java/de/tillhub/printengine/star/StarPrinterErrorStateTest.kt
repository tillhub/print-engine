package de.tillhub.printengine.star

import de.tillhub.printengine.data.PrinterState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class StarPrinterErrorStateTest : FunSpec({

    test("code") {
        StarPrinterErrorState.fromCode(-1) shouldBe StarPrinterErrorState.Unknown
        StarPrinterErrorState.fromCode(1000) shouldBe StarPrinterErrorState.DeviceHasError
        StarPrinterErrorState.fromCode(1001) shouldBe StarPrinterErrorState.PrinterHoldingPaper
        StarPrinterErrorState.fromCode(1002) shouldBe StarPrinterErrorState.PrintingTimeout
        StarPrinterErrorState.fromCode(2000) shouldBe StarPrinterErrorState.BluetoothUnavailable
        StarPrinterErrorState.fromCode(2001) shouldBe StarPrinterErrorState.NetworkUnavailable
        StarPrinterErrorState.fromCode(2002) shouldBe StarPrinterErrorState.UsbUnavailable
    }

    test("convert") {
        StarPrinterErrorState.convert(StarPrinterErrorState.Unknown) shouldBe
                PrinterState.Error.Unknown
        StarPrinterErrorState.convert(StarPrinterErrorState.DeviceHasError) shouldBe
                PrinterState.Error.AbnormalCommunication
        StarPrinterErrorState.convert(StarPrinterErrorState.PrinterHoldingPaper) shouldBe
                PrinterState.Error.PaperJam
        StarPrinterErrorState.convert(StarPrinterErrorState.PrintingTimeout) shouldBe
                PrinterState.Error.PrintingUnfinished
        StarPrinterErrorState.convert(StarPrinterErrorState.BluetoothUnavailable) shouldBe
                PrinterState.Error.NotAvailable
        StarPrinterErrorState.convert(StarPrinterErrorState.NetworkUnavailable) shouldBe
                PrinterState.Error.NotAvailable
        StarPrinterErrorState.convert(StarPrinterErrorState.UsbUnavailable) shouldBe
                PrinterState.Error.NotAvailable
    }
})
