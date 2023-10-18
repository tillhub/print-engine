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
import kotlinx.coroutines.flow.first

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
            every { updatePrinterState() } returns 1
        }
        serviceVersion = mockk()
        printerController = SunmiPrinterController(printerService, serviceVersion)
    }

    test("sendRawData") {
        val rawData = RawPrinterData("raw_data".toByteArray())
        printerController.sendRawData(rawData)

        verify(exactly = 1) {
            printerService.sendRAWData(rawData.bytes, any())
        }
    }

    test("observePrinterState") {
        printerController.observePrinterState().first() shouldBe PrinterState.PrinterNotDetected
    }

    test("setFontSize") {
        printerController.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)

        verify {
            printerService.setFontSize(20F, any())
        }
    }

    test("printText") {
        printerController.printText("text_to_print")

        verify {
            printerService.printText("text_to_print", any())
            printerService.lineWrap(1, any())
        }
    }

    test("printBarcode") {
        printerController.printBarcode("barcode")

        verify(ordering = Ordering.ORDERED) {
            printerService.setAlignment(1, any())
            printerService.printBarCode("barcode", 8, 100, 2, 2, any())
            printerService.setAlignment(0, any())
            printerService.lineWrap(1, any())
        }
    }

    test("printQr") {
        printerController.printQr("qr_code")

        verify(ordering = Ordering.ORDERED) {
            printerService.setAlignment(1, any())
            printerService.printQRCode("qr_code", 3, 0, any())
            printerService.setAlignment(0, any())
            printerService.lineWrap(1, any())
        }
    }

    test("printImage") {
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        printerController.printImage(bitmap)

        verify(ordering = Ordering.ORDERED) {
            printerService.setAlignment(1, any())
            printerService.printBitmapCustom(bitmap, 2, any())
            printerService.setAlignment(0, any())
            printerService.lineWrap(1, any())
        }
    }

    test("feedPaper") {
        printerController.feedPaper()

        verify {
            printerService.autoOutPaper(any())
        }
    }

    test("feedPaper exception") {
        every { printerService.autoOutPaper(any()) } throws RemoteException()

        printerController.feedPaper()

        verify {
            printerService.lineWrap(3, any())
        }
    }

    test("cutPaper") {
        printerController.cutPaper()

        verify {
            printerService.cutPaper(any())
        }
    }
})
