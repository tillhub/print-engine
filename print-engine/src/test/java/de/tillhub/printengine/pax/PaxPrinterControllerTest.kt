package de.tillhub.printengine.pax

import android.graphics.Bitmap
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.barcode.BarcodeType
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.data.RawPrinterData
import de.tillhub.printengine.html.FeedString
import de.tillhub.printengine.html.FontSize
import de.tillhub.printengine.html.HtmlUtils
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.shouldBe
import io.mockk.Ordering
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

@RobolectricTest
internal class PaxPrinterControllerTest : DescribeSpec({

    lateinit var bitmap: Bitmap
    lateinit var printerState: MutableStateFlow<PrinterState>
    lateinit var printService: DirectPrintService
    lateinit var barcodeEncoder: BarcodeEncoder
    lateinit var target: PaxPrinterController

    beforeSpec {
        bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
    }

    beforeTest {
        printerState = MutableStateFlow(PrinterState.CheckingForPrinter)
        barcodeEncoder = mockk {
            every { encodeAsBitmap(any(), any(), any(), any()) } returns bitmap
        }
        printService = mockk {
            every { checkStatus(any()) } just runs
            every { print(any(), any(), any()) } just runs
        }

        target = PaxPrinterController(
            printService = printService,
            printerState = printerState,
            barcodeEncoder = barcodeEncoder
        )
    }

    afterSpec {
        bitmap.recycle()
    }

    it("getPrinterInfo") {
        target.getPrinterInfo() shouldBe PrinterInfo(
            serialNumber = "n/a",
            deviceModel = "A920",
            printerVersion = "n/a",
            printerPaperSpec = PrintingPaperSpec.PaxPaper56mm,
            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
            printerHead = "n/a",
            printedDistance = 0,
            serviceVersion = PrinterServiceVersion.Unknown
        )
    }

    it("observePrinterState") {
        target.observePrinterState().first() shouldBe PrinterState.CheckingForPrinter

        verify(exactly = 1) { printService.checkStatus(any()) }
    }

    it("setIntensity and printText") {
        val payload = HtmlUtils.transformToHtml(
            text = HtmlUtils.monospaceText("text_to_print", FontSize.PAX.value) + "\n",
            includeStyle = true
        )

        target.setIntensity(PrintingIntensity.DARKEST)

        target.printText("text_to_print")
        target.start()

        verify {
            printService.print(payload, 100, any())
        }
    }

    it("sendRawData") {
        val payload = HtmlUtils.transformToHtml(
            text = HtmlUtils.monospaceText("raw_data", FontSize.PAX.value) + "\n",
            includeStyle = true
        )

        val rawData = RawPrinterData("raw_data".toByteArray())
        target.sendRawData(rawData)
        target.start()

        verify(exactly = 1) {
            printService.print(payload, 50, any())
        }
    }

    it("printBarcode") {
        val payload = HtmlUtils.transformToHtml(
            text = StringBuilder().apply {
                append(HtmlUtils.generateImageHtml(bitmap))
                appendLine(HtmlUtils.monospaceText(HtmlUtils.singleLineCenteredText("barcode")))
            }.toString(),
            includeStyle = true
        )

        target.printBarcode("barcode")
        target.start()

        verify(ordering = Ordering.ORDERED) {
            barcodeEncoder.encodeAsBitmap("barcode", BarcodeType.CODE_128, 435, 140)
            printService.print(payload, 50, any())
        }
    }

    it("printQr") {
        val payload = HtmlUtils.transformToHtml(
            text = StringBuilder().apply {
                append(HtmlUtils.generateImageHtml(bitmap))
                appendLine(
                    HtmlUtils.monospaceText(
                        HtmlUtils.singleLineCenteredText("qr_code")
                    )
                )
            }.toString(),
            includeStyle = true
        )

        target.printQr("qr_code")
        target.start()

        verify(ordering = Ordering.ORDERED) {
            barcodeEncoder.encodeAsBitmap("qr_code", BarcodeType.QR_CODE, 220, 220)
            printService.print(payload, 50, any())
        }
    }

    it("printImage") {
        val payload = HtmlUtils.transformToHtml(HtmlUtils.generateImageHtml(bitmap), true)
        target.printImage(bitmap)
        target.start()

        verify(exactly = 1) {
            printService.print(payload, 50, any())
        }
    }

    it("feedPaper") {
        val payload = HtmlUtils.transformToHtml(FeedString.PAX.value, true)

        target.feedPaper()
        target.start()

        verify(exactly = 1) {
            printService.print(payload, 50, any())
        }
    }

    it("full print") {
        val payload = HtmlUtils.transformToHtml(
            text = StringBuilder().apply {
                appendLine(HtmlUtils.monospaceText("start line", FontSize.PAX.value))
                append(HtmlUtils.generateImageHtml(bitmap))
                appendLine(HtmlUtils.monospaceText(HtmlUtils.singleLineCenteredText("barcode"), FontSize.PAX.value))
                append(HtmlUtils.generateImageHtml(bitmap))
                appendLine(HtmlUtils.monospaceText(HtmlUtils.singleLineCenteredText("qr_code"), FontSize.PAX.value))
                append(HtmlUtils.generateImageHtml(bitmap))
                appendLine(HtmlUtils.monospaceText("end line", FontSize.PAX.value))
                append(FeedString.PAX.value)
            }.toString(),
            includeStyle = true
        )

        target.printText("start line")
        target.printBarcode("barcode")
        target.printQr("qr_code")
        target.printImage(bitmap)
        target.printText("end line")
        target.feedPaper()
        target.start()

        verify(exactly = 1) {
            printService.print(payload, 50, any())
        }
    }
})
