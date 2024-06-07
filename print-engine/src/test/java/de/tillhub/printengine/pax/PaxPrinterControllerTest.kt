package de.tillhub.printengine.pax

import android.graphics.Bitmap
import com.pax.dal.IPrinter
import com.pax.dal.entity.EFontTypeAscii
import com.pax.dal.entity.EFontTypeExtCode
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.barcode.BarcodeType
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.data.RawPrinterData
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.shouldBe
import io.mockk.Ordering
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

@RobolectricTest
internal class PaxPrinterControllerTest : FunSpec({

    lateinit var bitmap: Bitmap
    lateinit var printerService: IPrinter
    lateinit var printerState: MutableStateFlow<PrinterState>
    lateinit var barcodeEncoder: BarcodeEncoder
    lateinit var printerController: PrinterController

    beforeSpec {
        bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
    }

    beforeTest {
        printerService = mockk {
            every { init() } just Runs
            every { fontSet(any(), any()) } just Runs
            every { printStr(any(), any()) } just Runs
            every { step(any()) } just Runs
            every { printBitmap(any()) } just Runs
            every { setGray(any()) } just Runs
            every { cutPaper(any()) } just Runs
            every { start() } returns 0
            every { status } returns 0
        }
        printerState = MutableStateFlow(PrinterState.CheckingForPrinter)
        barcodeEncoder = mockk {
            every { encodeAsBitmap(any(), any(), any(), any()) } returns bitmap
        }
        printerController = PaxPrinterController(printerService, printerState, barcodeEncoder)
    }

    afterSpec {
        bitmap.recycle()
    }

    test("observePrinterState") {
        every { printerService.status } returns 0
        printerController.observePrinterState().first() shouldBe PrinterState.Connected

        every { printerService.status } returns 1
        printerController.start()
        printerController.observePrinterState().first() shouldBe PrinterState.Busy

        every { printerService.status } returns 2
        printerController.start()
        printerController.observePrinterState().first() shouldBe PrinterState.Error.OutOfPaper

        every { printerService.status } returns 3
        printerController.start()
        printerController.observePrinterState().first() shouldBe PrinterState.Error.Pax.FormatPrintDataPacketError

        every { printerService.status } returns 4
        printerController.start()
        printerController.observePrinterState().first() shouldBe PrinterState.Error.Malfunctions

        every { printerService.status } returns 8
        printerController.start()
        printerController.observePrinterState().first() shouldBe PrinterState.Error.Overheated

        every { printerService.status } returns 9
        printerController.start()
        printerController.observePrinterState().first() shouldBe PrinterState.Error.VoltageTooLow

        every { printerService.status } returns 240
        printerController.start()
        printerController.observePrinterState().first() shouldBe PrinterState.Error.PrintingUnfinished

        every { printerService.status } returns 252
        printerController.start()
        printerController.observePrinterState().first() shouldBe PrinterState.Error.Pax.NotInstalledFontLibrary

        every { printerService.status } returns 254
        printerController.start()
        printerController.observePrinterState().first() shouldBe PrinterState.Error.Pax.DataPackageTooLong
    }

    test("getPrinterInfo") {
        printerController.getPrinterInfo() shouldBe PrinterInfo(
            serialNumber = "n/a",
            deviceModel = "A920",
            printerVersion = "n/a",
            printerPaperSpec = PrintingPaperSpec.PAX_PAPER_56MM,
            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
            printerHead = "n/a",
            printedDistance = 0,
            serviceVersion = PrinterServiceVersion.Unknown
        )
    }

    test("setFontSize") {
        printerController.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)

        verify {
            printerService.fontSet(EFontTypeAscii.FONT_12_24, EFontTypeExtCode.FONT_16_16)
        }
    }

    test("printText") {
        printerController.printText("text_to_print")

        verify {
            printerService.printStr("text_to_print\n", "UTF-8")
        }
    }

    test("printBarcode") {
        printerController.printBarcode("barcode")

        verify(ordering = Ordering.ORDERED) {
            barcodeEncoder.encodeAsBitmap("barcode", BarcodeType.CODE_128, 500, 150)
            printerService.step(20)
            printerService.printBitmap(bitmap)
            printerService.printStr("            barcode             ", "UTF-8")
        }
    }

    test("printQr") {
        printerController.printQr("qr_code")

        verify(ordering = Ordering.ORDERED) {
            barcodeEncoder.encodeAsBitmap("qr_code", BarcodeType.QR_CODE, 500, 500)
            printerService.step(20)
            printerService.printBitmap(bitmap)
            printerService.printStr("            qr_code             ", "UTF-8")
        }
    }

    test("printImage") {
        printerController.printImage(bitmap)

        verify(exactly = 1) {
            printerService.printBitmap(bitmap)
        }
    }

    test("sendRawData") {
        val rawData = RawPrinterData("raw_data".toByteArray())
        printerController.sendRawData(rawData)

        verify(exactly = 1) {
            printerController.printText("raw_data")
        }
    }

    test("feedPaper") {
        printerController.feedPaper()

        verify(exactly = 1) {
            printerService.step(180)
        }
    }

    test("cutPaper") {
        printerController.cutPaper()

        verify(exactly = 1) {
            printerService.cutPaper(0)
        }
    }

    test("setIntensity") {
        printerController.setIntensity(PrintingIntensity.DEFAULT)
        printerController.setIntensity(PrintingIntensity.LIGHT)
        printerController.setIntensity(PrintingIntensity.DARK)
        printerController.setIntensity(PrintingIntensity.DARKER)
        printerController.setIntensity(PrintingIntensity.DARKEST)

        verify(ordering = Ordering.ORDERED) {
            printerService.setGray(1)
            printerService.setGray(50)
            printerService.setGray(150)
            printerService.setGray(250)
            printerService.setGray(500)
        }
    }

    test("start") {
        printerController.start()

        verify(ordering = Ordering.ORDERED) {
            printerService.start()
            printerService.init()
        }
    }
})
