package de.tillhub.printengine.sunmi

import android.graphics.Bitmap
import android.os.RemoteException
import com.sunmi.peripheral.printer.SunmiPrinterService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.RawPrinterData
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.shouldBe
import io.mockk.*

@RobolectricTest
class SunmiPrinterControllerTest : FunSpec({

    lateinit var printerService: SunmiPrinterService
    lateinit var serviceVersion: PrinterServiceVersion
    lateinit var printerController: PrinterController

    beforeTest {
        printerService = mockk {
            every { sendRAWData(any(), any()) } just Runs
            every { setFontSize(any(), any()) } just Runs
            every { printText(any(), any()) } just Runs
            every { lineWrap(any(), any()) } just Runs
            every { setAlignment(any(), any()) } just Runs
            every { printBarCode(any(), any(), any(), any(), any(), any()) } just Runs
            every { printQRCode(any(), any(), any(), any()) } just Runs
            every { printBitmapCustom(any(), any(), any()) } just Runs
            every { autoOutPaper(any()) } just Runs
            every { cutPaper(any()) } just Runs
        }
        serviceVersion = mockk()
        printerController = SunmiPrinterController(printerService, serviceVersion)
    }

    test("sendRawData") {
        val rawData = RawPrinterData("raw_data".toByteArray())
        printerController.sendRawData(rawData)

        verify(exactly = 1) {
            printerService.sendRAWData(rawData.bytes, null)
        }
    }

    test("getPrinterState") {
        every { printerService.updatePrinterState() } returns -1
        printerController.getPrinterState() shouldBe PrinterState.Error.Unknown

        every { printerService.updatePrinterState() } returns 1
        printerController.getPrinterState() shouldBe PrinterState.Connected

        every { printerService.updatePrinterState() } returns 2
        printerController.getPrinterState() shouldBe PrinterState.Preparing

        every { printerService.updatePrinterState() } returns 3
        printerController.getPrinterState() shouldBe PrinterState.Error.AbnormalCommunication

        every { printerService.updatePrinterState() } returns 4
        printerController.getPrinterState() shouldBe PrinterState.Error.OutOfPaper

        every { printerService.updatePrinterState() } returns 5
        printerController.getPrinterState() shouldBe PrinterState.Error.Overheated

        every { printerService.updatePrinterState() } returns 6
        printerController.getPrinterState() shouldBe PrinterState.Error.CoverNotClosed

        every { printerService.updatePrinterState() } returns 7
        printerController.getPrinterState() shouldBe PrinterState.Error.PaperCutterAbnormal

        every { printerService.updatePrinterState() } returns 8
        printerController.getPrinterState() shouldBe PrinterState.Connected

        every { printerService.updatePrinterState() } returns 9
        printerController.getPrinterState() shouldBe PrinterState.Error.BlackMarkNotFound

        every { printerService.updatePrinterState() } returns 505
        printerController.getPrinterState() shouldBe PrinterState.PrinterNotDetected

        every { printerService.updatePrinterState() } returns 507
        printerController.getPrinterState() shouldBe PrinterState.Error.FirmwareUpgradeFailed
    }

    test("setFontSize") {
        printerController.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)

        verify {
            printerService.setFontSize(20F, null)
        }
    }

    test("printText") {
        printerController.printText("text_to_print")

        verify {
            printerService.printText("text_to_print", null)
            printerService.lineWrap(1, null)
        }
    }

    test("printBarcode") {
        printerController.printBarcode("barcode")

        verify(ordering = Ordering.ORDERED) {
            printerService.setAlignment(1, null)
            printerService.printBarCode("barcode", 8, 100, 2, 2, null)
            printerService.setAlignment(0, null)
            printerService.lineWrap(1, null)
        }
    }

    test("printQr") {
        printerController.printQr("qr_code")

        verify(ordering = Ordering.ORDERED) {
            printerService.setAlignment(1, null)
            printerService.printQRCode("qr_code", 3, 0, null)
            printerService.setAlignment(0, null)
            printerService.lineWrap(1, null)
        }
    }

    test("printImage") {
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        printerController.printImage(bitmap)

        verify(ordering = Ordering.ORDERED) {
            printerService.setAlignment(1, null)
            printerService.printBitmapCustom(bitmap, 2, null)
            printerService.setAlignment(0, null)
            printerService.lineWrap(1, null)
        }
    }

    test("feedPaper") {
        printerController.feedPaper()

        verify {
            printerService.autoOutPaper(null)
        }
    }

    test("feedPaper exception") {
        every { printerService.autoOutPaper(any()) } throws RemoteException()

        printerController.feedPaper()

        verify {
            printerService.lineWrap(3, null)
        }
    }

    test("cutPaper") {
        printerController.cutPaper()

        verify {
            printerService.cutPaper(null)
        }
    }
})
