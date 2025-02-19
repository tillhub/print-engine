package de.tillhub.printengine.pax

import android.graphics.Bitmap
import android.os.Message
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.barcode.BarcodeType
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.data.RawPrinterData
import de.tillhub.printengine.html.HtmlUtils
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.shouldBe
import io.mockk.Ordering
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

@RobolectricTest
internal class PaxPrinterControllerTest : DescribeSpec({

    lateinit var bitmap: Bitmap
    lateinit var printerState: MutableStateFlow<PrinterState>
    lateinit var paxPrinterConnector: PaxPrinterConnector
    lateinit var barcodeEncoder: BarcodeEncoder
    lateinit var target: PaxPrinterController

    var call: Int? = null
    val htmls: MutableList<String> = mutableListOf()
    var grey: Int? = null

    beforeSpec {
        bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
    }

    beforeTest {
        printerState = MutableStateFlow(PrinterState.CheckingForPrinter)
        paxPrinterConnector = mockk {
            every { sendMessage(any()) } answers {
                val message = firstArg<Message>()
                call = message.what
                if (call == 1) {
                    htmls.add(message.data.getString("html").orEmpty())
                    grey = message.data.getInt("grey")
                }
            }
        }
        barcodeEncoder = mockk {
            every { encodeAsBitmap(any(), any(), any(), any()) } returns bitmap
        }
    }

    afterTest {

        htmls.clear()
    }

    afterSpec {
        bitmap.recycle()
    }

    it("getPrinterInfo") {
        target = PaxPrinterController(
            paxPrinterConnector = paxPrinterConnector,
            printerState = printerState,
            barcodeEncoder = barcodeEncoder
        )

        target.getPrinterInfo() shouldBe PrinterInfo(
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

    it("observePrinterState") {
        target = PaxPrinterController(
            paxPrinterConnector = paxPrinterConnector,
            printerState = printerState,
            barcodeEncoder = barcodeEncoder
        )

        target.observePrinterState().first() shouldBe PrinterState.CheckingForPrinter
    }

    it("setIntensity") {
        target = PaxPrinterController(
            paxPrinterConnector = paxPrinterConnector,
            printerState = printerState,
            barcodeEncoder = barcodeEncoder,
            batchPrint = false
        )

        target.setIntensity(PrintingIntensity.DARKEST)

        target.printText("text_to_print")

        call shouldBe 1
        htmls[0] shouldBe HtmlUtils.transformToHtml(
            text = HtmlUtils.monospaceText("text_to_print", 13),
            includeStyle = true
        )
        grey shouldBe 100
    }

    describe("batchPrint = false") {
        beforeTest {
            target = PaxPrinterController(
                paxPrinterConnector = paxPrinterConnector,
                printerState = printerState,
                barcodeEncoder = barcodeEncoder,
                batchPrint = false
            )
        }

        it("sendRawData") {
            val rawData = RawPrinterData("raw_data".toByteArray())
            target.sendRawData(rawData)

            call shouldBe 1
            htmls[0] shouldBe HtmlUtils.transformToHtml(
                text = HtmlUtils.monospaceText("raw_data", 13),
                includeStyle = true
            )
            grey shouldBe 50

            verify {
                paxPrinterConnector.sendMessage(any())
            }
        }

        it("printText") {
            target.printText("text_to_print")

            call shouldBe 1
            htmls[0] shouldBe HtmlUtils.transformToHtml(
                text = HtmlUtils.monospaceText("text_to_print", 13),
                includeStyle = true
            )
            grey shouldBe 50

            verify {
                paxPrinterConnector.sendMessage(any())
            }
        }

        it("printBarcode") {
            target.printBarcode("barcode")

            call shouldBe 1
            htmls[0] shouldBe HtmlUtils.transformToHtml(
                text = HtmlUtils.generateImageHtml(bitmap),
                includeStyle = true
            )
            htmls[1] shouldBe HtmlUtils.transformToHtml(
                text = HtmlUtils.monospaceText(HtmlUtils.singleLineCenteredText("barcode"), 13),
                includeStyle = true
            )
            grey shouldBe 50

            verify(ordering = Ordering.ORDERED) {
                barcodeEncoder.encodeAsBitmap(
                    "barcode",
                    BarcodeType.CODE_128,
                    220,
                    70
                )
                paxPrinterConnector.sendMessage(any())
            }
        }

        it("printQr") {
            target.printQr("qr_code")

            call shouldBe 1
            htmls[0] shouldBe HtmlUtils.transformToHtml(
                text = HtmlUtils.generateImageHtml(bitmap),
                includeStyle = true
            )
            htmls[1] shouldBe HtmlUtils.transformToHtml(
                text = HtmlUtils.monospaceText(HtmlUtils.singleLineCenteredText("qr_code"), 13),
                includeStyle = true
            )
            grey shouldBe 50

            verify(ordering = Ordering.ORDERED) {
                barcodeEncoder.encodeAsBitmap(
                    "qr_code",
                    BarcodeType.QR_CODE,
                    220,
                    220
                )
                paxPrinterConnector.sendMessage(any())
            }
        }

        it("printImage") {
            target.printImage(bitmap)

            call shouldBe 1
            htmls[0] shouldBe HtmlUtils.transformToHtml(
                text = HtmlUtils.generateImageHtml(bitmap),
                includeStyle = true
            )
            grey shouldBe 50

            verify {
                paxPrinterConnector.sendMessage(any())
            }
        }

        it("feedPaper") {
            target.feedPaper()

            call shouldBe 1
            htmls[0] shouldBe "<br />"
            grey shouldBe 50

            verify {
                paxPrinterConnector.sendMessage(any())
            }
        }
    }

    describe("batchPrint = true") {
        beforeTest {
            target = PaxPrinterController(
                paxPrinterConnector = paxPrinterConnector,
                printerState = printerState,
                barcodeEncoder = barcodeEncoder,
                batchPrint = true
            )
        }

        it("full print") {
            val payload = HtmlUtils.transformToHtml(
                StringBuilder().apply {
                    appendLine(HtmlUtils.monospaceText("start line", 13))
                    append(HtmlUtils.generateImageHtml(bitmap))
                    appendLine(HtmlUtils.monospaceText(HtmlUtils.singleLineCenteredText("barcode"), 13))
                    append(HtmlUtils.generateImageHtml(bitmap))
                    appendLine(HtmlUtils.monospaceText(HtmlUtils.singleLineCenteredText("qr_code"), 13))
                    append(HtmlUtils.generateImageHtml(bitmap))
                    appendLine(HtmlUtils.monospaceText("end line", 13))
                    append(HtmlUtils.FEED_PAPER_SMALL)
                }.toString(),
                true
            )

            target.printText("start line")
            target.printBarcode("barcode")
            target.printQr("qr_code")
            target.printImage(bitmap)
            target.printText("end line")
            target.feedPaper()
            target.start()

            call shouldBe 1
            htmls[0] shouldBe payload
            grey shouldBe 50

            verify {
                paxPrinterConnector.sendMessage(any())
            }
        }
    }
})
