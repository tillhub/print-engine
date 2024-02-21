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
import io.kotest.core.spec.style.DescribeSpec
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
class VerifonePrintControllerTest : DescribeSpec({

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
    }

    afterSpec {
        bitmap.recycle()
    }

    describe("batchPrint = false") {
        beforeTest {
            printerController = VerifonePrintController(
                printManager = printManager,
                printerState = printerState,
                barcodeEncoder = barcodeEncoder,
                batchPrint = false
            )
        }

        it("observePrinterState") {
            printerController.observePrinterState().first() shouldBe PrinterState.CheckingForPrinter
        }

        it("getPrinterInfo") {
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

        it("sendRawData") {
            val rawData = RawPrinterData("raw_data".toByteArray())
            printerController.sendRawData(rawData)

            verify(exactly = 1) {
                printManager.printString(any(), VerifoneUtils.transformToHtml("raw_data"), Printer.PRINTER_NO_CUTTER_LINE_FEED)
            }
        }

        it("printText") {
            printerController.printText("text_to_print")

            verify(exactly = 1) {
                printManager.printString(any(), VerifoneUtils.transformToHtml("text_to_print"), Printer.PRINTER_NO_CUTTER_LINE_FEED)
            }
        }

        it("printBarcode") {
            printerController.printBarcode("barcode")

            verify(ordering = Ordering.ORDERED) {
                barcodeEncoder.encodeAsBitmap("barcode", BarcodeType.CODE_128, 420, 140)
                printManager.printBitmap(any(), bitmap, Printer.PRINTER_NO_CUTTER_LINE_FEED)
                printManager.printString(any(), VerifoneUtils.transformToHtml(VerifoneUtils.singleLineCenteredText("barcode")), Printer.PRINTER_NO_CUTTER_LINE_FEED)
            }
        }

        it("printQr") {
            printerController.printQr("qr_code")

            verify(ordering = Ordering.ORDERED) {
                barcodeEncoder.encodeAsBitmap("qr_code", BarcodeType.QR_CODE, 420, 420)
                printManager.printBitmap(any(), bitmap, Printer.PRINTER_NO_CUTTER_LINE_FEED)
                printManager.printString(any(), VerifoneUtils.transformToHtml(VerifoneUtils.singleLineCenteredText("qr_code")), Printer.PRINTER_NO_CUTTER_LINE_FEED)
            }
        }

        it("printImage") {
            printerController.printImage(bitmap)

            verify(exactly = 1) {
                printManager.printBitmap(any(), bitmap, Printer.PRINTER_NO_CUTTER_LINE_FEED)
            }
        }

        it("feedPaper") {
            printerController.feedPaper()

            verify(exactly = 1) {
                printManager.printString(any(), "", Printer.PRINTER_NO_CUT)
            }
        }

        it("cutPaper") {
            printerController.cutPaper()

            verify(exactly = 1) {
                printManager.printString(any(), "", Printer.PRINTER_FULL_CUT)
            }
        }
    }

    describe("batchPrint = true") {
        beforeTest {
            printerController = VerifonePrintController(
                printManager = printManager,
                printerState = printerState,
                barcodeEncoder = barcodeEncoder,
                batchPrint = true
            )
        }

        it("observePrinterState") {
            printerController.observePrinterState().first() shouldBe PrinterState.CheckingForPrinter
        }

        it("getPrinterInfo") {
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

        it("sendRawData") {
            val payload = VerifoneUtils.wrapTableRows(
                VerifoneUtils.transformToTableRowHtml("raw_data")
            )

            val rawData = RawPrinterData("raw_data".toByteArray())
            printerController.sendRawData(rawData)
            printerController.start()

            verify(exactly = 1) {
                printManager.printString(any(), payload, Printer.PRINTER_NO_CUTTER_LINE_FEED)
            }
        }

        it("printText") {
            val payload = VerifoneUtils.wrapTableRows(
                VerifoneUtils.transformToTableRowHtml("text_to_print")
            )

            printerController.printText("text_to_print")
            printerController.start()

            verify(exactly = 1) {
                printManager.printString(any(), payload, Printer.PRINTER_NO_CUTTER_LINE_FEED)
            }
        }

        it("printBarcode") {
            val payload = VerifoneUtils.wrapTableRows(
                VerifoneUtils.transformToHtml(bitmap) +
                VerifoneUtils.transformToCenteredTableRowHtml("barcode")
            )

            printerController.printBarcode("barcode")
            printerController.start()

            verify(ordering = Ordering.ORDERED) {
                barcodeEncoder.encodeAsBitmap("barcode", BarcodeType.CODE_128, 420, 140)
                printManager.printString(any(), payload, Printer.PRINTER_NO_CUTTER_LINE_FEED)
            }
        }

        it("printQr") {
            val payload = VerifoneUtils.wrapTableRows(
                VerifoneUtils.transformToHtml(bitmap) +
                VerifoneUtils.transformToCenteredTableRowHtml("qr_code")
            )

            printerController.printQr("qr_code")
            printerController.start()

            verify(ordering = Ordering.ORDERED) {
                barcodeEncoder.encodeAsBitmap("qr_code", BarcodeType.QR_CODE, 420, 420)
                printManager.printString(any(), payload, Printer.PRINTER_NO_CUTTER_LINE_FEED)
            }
        }

        it("printImage") {
            val payload = VerifoneUtils.wrapTableRows(VerifoneUtils.transformToHtml(bitmap))

            printerController.printImage(bitmap)
            printerController.start()

            verify(exactly = 1) {
                printManager.printString(any(), payload, Printer.PRINTER_NO_CUTTER_LINE_FEED)
            }
        }

        it("feedPaper") {
            val payload = VerifoneUtils.wrapTableRows(VerifoneUtils.transformToTableRowHtml(""))

            printerController.feedPaper()
            printerController.start()

            verify(exactly = 1) {
                printManager.printString(any(), payload, Printer.PRINTER_NO_CUTTER_LINE_FEED)
            }
        }

        it("full print, no cut") {
            val payload = VerifoneUtils.wrapTableRows(
                VerifoneUtils.transformToTableRowHtml("start line") +
                VerifoneUtils.transformToHtml(bitmap) +
                VerifoneUtils.transformToCenteredTableRowHtml("barcode") +
                VerifoneUtils.transformToHtml(bitmap) +
                VerifoneUtils.transformToCenteredTableRowHtml("qr_code") +
                VerifoneUtils.transformToHtml(bitmap) +
                VerifoneUtils.transformToTableRowHtml("end line") +
                VerifoneUtils.transformToTableRowHtml("")
            )

            printerController.printText("start line")
            printerController.printBarcode("barcode")
            printerController.printQr("qr_code")
            printerController.printImage(bitmap)
            printerController.printText("end line")
            printerController.feedPaper()
            printerController.start()

            verify(exactly = 1) {
                printManager.printString(any(), payload, Printer.PRINTER_NO_CUTTER_LINE_FEED)
            }
        }

        it("full print, full cut") {
            val payload = VerifoneUtils.wrapTableRows(
                VerifoneUtils.transformToTableRowHtml("start line") +
                VerifoneUtils.transformToHtml(bitmap) +
                VerifoneUtils.transformToCenteredTableRowHtml("barcode") +
                VerifoneUtils.transformToHtml(bitmap) +
                VerifoneUtils.transformToCenteredTableRowHtml("qr_code") +
                VerifoneUtils.transformToHtml(bitmap) +
                VerifoneUtils.transformToTableRowHtml("end line") +
                VerifoneUtils.transformToTableRowHtml("")
            )

            printerController.printText("start line")
            printerController.printBarcode("barcode")
            printerController.printQr("qr_code")
            printerController.printImage(bitmap)
            printerController.printText("end line")
            printerController.feedPaper()
            printerController.cutPaper()
            printerController.start()

            verify(exactly = 1) {
                printManager.printString(any(), payload, Printer.PRINTER_FULL_CUT)
            }
        }
    }
})
