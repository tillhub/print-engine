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
import de.tillhub.printengine.html.FeedString
import de.tillhub.printengine.html.FontSize
import de.tillhub.printengine.html.HtmlUtils
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
internal class VerifonePrintControllerTest : DescribeSpec({

    lateinit var bitmap: Bitmap
    lateinit var printManager: DirectPrintManager
    lateinit var printerState: MutableStateFlow<PrinterState>
    lateinit var barcodeEncoder: BarcodeEncoder
    lateinit var printerController: PrinterController

    beforeSpec {
        bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
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

        printerController = VerifonePrintController(
            printManager = printManager,
            printerState = printerState,
            barcodeEncoder = barcodeEncoder
        )
    }

    afterSpec {
        bitmap.recycle()
    }

    it("observePrinterState") {
        printerController.observePrinterState().first() shouldBe PrinterState.CheckingForPrinter
    }

    it("getPrinterInfo") {
        printerController.getPrinterInfo() shouldBe PrinterInfo(
            serialNumber = "n/a",
            deviceModel = "Verifone T630c",
            printerVersion = "n/a",
            printerPaperSpec = PrintingPaperSpec.VerifonePaper56mm,
            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
            printerHead = "n/a",
            printedDistance = 0,
            serviceVersion = PrinterServiceVersion.Unknown
        )
    }

    it("sendRawData") {
        val payload = HtmlUtils.transformToHtml(
            HtmlUtils.monospaceText(
                "raw_data",
                FontSize.VERIFONE.value
            ) + "\n"
        )

        val rawData = RawPrinterData("raw_data".toByteArray())
        printerController.sendRawData(rawData)
        printerController.start()

        verify(exactly = 1) {
            printManager.printString(any(), payload, Printer.PRINTER_NO_CUTTER_LINE_FEED)
        }
    }

    it("printText") {
        val payload = HtmlUtils.transformToHtml(
            HtmlUtils.monospaceText(
                "text_to_print",
                FontSize.VERIFONE.value
            ) + "\n"
        )

        printerController.printText("text_to_print")
        printerController.start()

        verify(exactly = 1) {
            printManager.printString(any(), payload, Printer.PRINTER_NO_CUTTER_LINE_FEED)
        }
    }

    it("printBarcode") {
        val payload = HtmlUtils.transformToHtml(
            StringBuilder().apply {
                append(HtmlUtils.generateImageHtml(bitmap))
                appendLine(
                    HtmlUtils.monospaceText(
                        HtmlUtils.singleLineCenteredText("barcode"),
                        FontSize.VERIFONE.value
                    )
                )
            }.toString()
        )

        printerController.printBarcode("barcode")
        printerController.start()

        verify(ordering = Ordering.ORDERED) {
            barcodeEncoder.encodeAsBitmap("barcode", BarcodeType.CODE_128, 435, 140)
            printManager.printString(any(), payload, Printer.PRINTER_NO_CUTTER_LINE_FEED)
        }
    }

    it("printQr") {
        val payload = HtmlUtils.transformToHtml(
            StringBuilder().apply {
                append(HtmlUtils.generateImageHtml(bitmap))
                appendLine(
                    HtmlUtils.monospaceText(
                        HtmlUtils.singleLineCenteredText("qr_code"),
                        FontSize.VERIFONE.value
                    )
                )
            }.toString()
        )

        printerController.printQr("qr_code")
        printerController.start()

        verify(ordering = Ordering.ORDERED) {
            barcodeEncoder.encodeAsBitmap("qr_code", BarcodeType.QR_CODE, 420, 420)
            printManager.printString(any(), payload, Printer.PRINTER_NO_CUTTER_LINE_FEED)
        }
    }

    it("printImage") {
        val payload = HtmlUtils.transformToHtml(HtmlUtils.generateImageHtml(bitmap))
        printerController.printImage(bitmap)
        printerController.start()

        verify(exactly = 1) {
            printManager.printString(any(), payload, Printer.PRINTER_NO_CUTTER_LINE_FEED)
        }
    }

    it("feedPaper") {
        val payload = HtmlUtils.transformToHtml(FeedString.VERIFONE.value)

        printerController.feedPaper()
        printerController.start()

        verify(exactly = 1) {
            printManager.printString(any(), payload, Printer.PRINTER_NO_CUTTER_LINE_FEED)
        }
    }

    it("full print, no cut") {
        val payload = HtmlUtils.transformToHtml(
            StringBuilder().apply {
                appendLine(HtmlUtils.monospaceText("start line", FontSize.VERIFONE.value))
                append(HtmlUtils.generateImageHtml(bitmap))
                appendLine(
                    HtmlUtils.monospaceText(
                        HtmlUtils.singleLineCenteredText("barcode"),
                        FontSize.VERIFONE.value
                    )
                )
                append(HtmlUtils.generateImageHtml(bitmap))
                appendLine(
                    HtmlUtils.monospaceText(
                        HtmlUtils.singleLineCenteredText("qr_code"),
                        FontSize.VERIFONE.value
                    )
                )
                append(HtmlUtils.generateImageHtml(bitmap))
                appendLine(HtmlUtils.monospaceText("end line", FontSize.VERIFONE.value))
                append(FeedString.VERIFONE.value)
            }.toString()
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
        val payload = HtmlUtils.transformToHtml(
            StringBuilder().apply {
                appendLine(HtmlUtils.monospaceText("start line", FontSize.VERIFONE.value))
                append(HtmlUtils.generateImageHtml(bitmap))
                appendLine(
                    HtmlUtils.monospaceText(
                        HtmlUtils.singleLineCenteredText("barcode"),
                        FontSize.VERIFONE.value
                    )
                )
                append(HtmlUtils.generateImageHtml(bitmap))
                appendLine(
                    HtmlUtils.monospaceText(
                        HtmlUtils.singleLineCenteredText("qr_code"),
                        FontSize.VERIFONE.value
                    )
                )
                append(HtmlUtils.generateImageHtml(bitmap))
                appendLine(HtmlUtils.monospaceText("end line", FontSize.VERIFONE.value))
                append(FeedString.VERIFONE.value)
            }.toString()
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
})
