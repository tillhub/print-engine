package de.tillhub.printengine.sunmi

import android.content.Context
import android.graphics.Bitmap
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.Printer
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.analytics.PrintAnalytics
import de.tillhub.printengine.data.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow

@RobolectricTest
class SunmiPrinterTest : DescribeSpec({

    lateinit var bitmap: Bitmap
    lateinit var footerBitmap: Bitmap
    lateinit var controller: PrinterController
    lateinit var service: PrintService
    lateinit var analytics: PrintAnalytics
    lateinit var printer: Printer

    beforeSpec {
        bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        footerBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
    }

    beforeTest {
        controller = mockk {
            every { getPrinterState() } returns PrinterState.Connected
            every { setFontSize(any()) } just Runs
            every { printText(any()) } just Runs
            every { printImage(any()) } just Runs
            every { printBarcode(any()) } just Runs
            every { printQr(any()) } just Runs
            every { sendRawData(any()) } just Runs
            every { feedPaper() } just Runs
            every { cutPaper() } just Runs
            coEvery { getPrinterInfo() } returns PrinterInfo(
                serialNumber = "n/a",
                deviceModel = "P9 pro",
                printerVersion = "n/a",
                printerPaperSpec = PrintingPaperSpec.SUNMI_PAPER_56MM,
                printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
                printerHead = "n/a",
                printedDistance = 0,
                serviceVersion = PrinterServiceVersion.Unknown
            )
        }
        service = mockk {
            every { printController } returns controller
            every { initPrinterService(any()) } just Runs
        }
        analytics = mockk {
            every { logPrintReceipt(any()) } just Runs
            every { logErrorPrintReceipt(any()) } just Runs
        }
        printer = SunmiPrinter(service, analytics)
    }

    afterSpec {
        bitmap.recycle()
    }

    describe("independent") {
        it("connect") {
            val context: Context = mockk()
            printer.connect(context)

            verify {
                service.initPrinterService(context)
            }
        }

        it("observeConnection") {
            val flow = MutableStateFlow(PrinterConnectionState.PrinterConnected)
            every { service.printerConnectionState } returns flow

            printer.observeConnection() shouldBe flow
        }

        it("getPrinterState") {
            printer.getPrinterState() shouldBe PrinterState.Connected
        }

        it("getPrinterInfo") {
            printer.getPrinterInfo() shouldBe PrinterResult.Success(
                PrinterInfo(
                    serialNumber = "n/a",
                    deviceModel = "P9 pro",
                    printerVersion = "n/a",
                    printerPaperSpec = PrintingPaperSpec.SUNMI_PAPER_56MM,
                    printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
                    printerHead = "n/a",
                    printedDistance = 0,
                    serviceVersion = PrinterServiceVersion.Unknown
                )
            )
        }
    }

    describe("printer enabled") {
        beforeTest {
            printer.enable()
        }

        it("printText") {
            printer.printText("text_to_print")

            verify {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printText("text_to_print")
            }
        }

        it("printReceipt without image") {
            printer.printReceipt("receipt_to_print", null)

            verify {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printText("receipt_to_print")
                analytics.logPrintReceipt("receipt_to_print")
            }

            verify(inverse = true) {
                controller.printImage(any())
            }
        }

        it("printReceipt with image") {
            printer.printReceipt("receipt_to_print", bitmap)

            verify {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printImage(bitmap)
                controller.printText("receipt_to_print")
                analytics.logPrintReceipt("receipt_to_print")
            }
        }

        it("printReceipt") {
            printer.printReceipt("raw_receipt_text", "barcode", bitmap, footerBitmap, "signature_qr_code")

            verify {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printImage(bitmap)
                controller.printText("raw_receipt_text")
                controller.printQr("signature_qr_code")
                controller.printBarcode("barcode")
                controller.printImage(footerBitmap)
                controller.feedPaper()
                analytics.logPrintReceipt("raw_receipt_text")
            }
        }

        it("print RawReceipt") {
            val rawReceipt = RawReceipt(RawPrinterData("raw_data".toByteArray()))
            printer.printReceipt(rawReceipt)

            verify {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.sendRawData(rawReceipt.rawData)
                analytics.logPrintReceipt("raw_data")
            }
        }

        it("feedPaper") {
            printer.feedPaper()

            verify {
                controller.feedPaper()
            }
        }

        it("cutPaper") {
            printer.cutPaper()

            verify {
                controller.cutPaper()
            }
        }
    }

    describe("printer disabled") {
        beforeTest {
            printer.disable()
        }

        it("printText") {
            printer.printText("text_to_print")

            verify(inverse = true) {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printText("text_to_print")
            }
        }

        it("printReceipt without image") {
            printer.printReceipt("receipt_to_print", null)

            verify(inverse = true) {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printText("receipt_to_print")
            }

            verify(inverse = true) {
                controller.printImage(any())
            }
        }

        it("printReceipt with image") {
            printer.printReceipt("receipt_to_print", bitmap)

            verify(inverse = true) {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printImage(bitmap)
                controller.printText("receipt_to_print")
            }
        }

        it("printReceipt") {
            printer.printReceipt("raw_receipt_text", "barcode", bitmap, footerBitmap, "signature_qr_code")

            verify(inverse = true) {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printImage(bitmap)
                controller.printText("raw_receipt_text")
                controller.printQr("signature_qr_code")
                controller.printBarcode("barcode")
                controller.printImage(footerBitmap)
                controller.feedPaper()
            }
        }

        it("print RawReceipt") {
            val rawReceipt = RawReceipt(RawPrinterData("raw_data".toByteArray()))
            printer.printReceipt(rawReceipt)

            verify(inverse = true) {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.sendRawData(rawReceipt.rawData)
            }
        }

        it("feedPaper") {
            printer.feedPaper()

            verify(inverse = true) {
                controller.feedPaper()
            }
        }

        it("cutPaper") {
            printer.cutPaper()

            verify(inverse = true) {
                controller.cutPaper()
            }
        }
    }
})
