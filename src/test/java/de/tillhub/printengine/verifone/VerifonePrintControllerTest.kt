package de.tillhub.printengine.verifone

import android.graphics.Bitmap
import com.verifone.peripherals.DirectPrintManager
import com.verifone.peripherals.Printer
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.barcode.BarcodeType
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
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
class VerifonePrintControllerTest : FunSpec({

    lateinit var bitmap: Bitmap
    lateinit var printManager: DirectPrintManager
    lateinit var printerState: MutableStateFlow<PrinterState>
    lateinit var barcodeEncoder: BarcodeEncoder
    lateinit var printerController: PrinterController

    beforeSpec {
        bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
    }

    beforeTest {
        printManager = mockk {
            every { printString(any(), any(), any()) } just Runs
            every { printBitmap(any(), any(), any()) } just Runs
        }
        printerState = MutableStateFlow(PrinterState.CheckingForPrinter)
        barcodeEncoder = mockk {
            every { encodeAsBitmap(any(), any(), any(), any()) } returns bitmap
        }
        printerController = VerifonePrintController(printManager, printerState, barcodeEncoder)
    }

    afterSpec {
        bitmap.recycle()
    }

    test("sendRawData") {
        val rawData = RawPrinterData("raw_data".toByteArray())
        printerController.sendRawData(rawData)

        verify(exactly = 1) {
            printManager.printString(any(), VerifoneUtils.transformToHtml("raw_data"), Printer.PRINTER_NO_CUTTER_LINE_FEED)
        }
    }

    test("observePrinterState") {
        printerController.observePrinterState().first() shouldBe PrinterState.CheckingForPrinter
    }

    test("printText") {
        printerController.printText("text_to_print")

        verify(exactly = 1) {
            printManager.printString(any(), VerifoneUtils.transformToHtml("text_to_print"), Printer.PRINTER_NO_CUTTER_LINE_FEED)
        }
    }

    test("printBarcode") {
        printerController.printBarcode("barcode")

        verify(ordering = Ordering.ORDERED) {
            barcodeEncoder.encodeAsBitmap("barcode", BarcodeType.CODE_128, 500, 150)
            printManager.printBitmap(any(), bitmap, Printer.PRINTER_NO_CUTTER_LINE_FEED)
            printManager.printString(any(), VerifoneUtils.transformToHtml(VerifoneUtils.singleLineCenteredText("barcode")), Printer.PRINTER_NO_CUTTER_LINE_FEED)
        }
    }

    test("printQr") {
        printerController.printQr("qr_code")

        verify(ordering = Ordering.ORDERED) {
            barcodeEncoder.encodeAsBitmap("qr_code", BarcodeType.QR_CODE, 500, 500)
            printManager.printBitmap(any(), bitmap, Printer.PRINTER_NO_CUTTER_LINE_FEED)
            printManager.printString(any(), VerifoneUtils.transformToHtml(VerifoneUtils.singleLineCenteredText("qr_code")), Printer.PRINTER_NO_CUTTER_LINE_FEED)
        }
    }

    test("printImage") {
        printerController.printImage(bitmap)

        verify(exactly = 1) {
            printManager.printBitmap(any(), bitmap, Printer.PRINTER_NO_CUTTER_LINE_FEED)
        }
    }

    test("feedPaper") {
        printerController.feedPaper()

        verify(exactly = 1) {
            printManager.printString(any(), "", Printer.PRINTER_NO_CUT)
        }
    }

    test("cutPaper") {
        printerController.cutPaper()

        verify(exactly = 1) {
            printManager.printString(any(), "", Printer.PRINTER_FULL_CUT)
        }
    }

    test("getPrinterInfo") {
        printerController.getPrinterInfo() shouldBe PrinterInfo(
            serialNumber = "n/a",
            deviceModel = "Verifone T630c",
            printerVersion = "n/a",
            printerPaperSpec = PrintingPaperSpec.PAX_PAPER_56MM,
            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
            printerHead = "n/a",
            printedDistance = 0,
            serviceVersion = PrinterServiceVersion.Unknown
        )
    }
})
