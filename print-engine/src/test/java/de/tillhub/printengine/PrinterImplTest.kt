package de.tillhub.printengine

import android.graphics.Bitmap
import de.tillhub.printengine.analytics.PrintAnalytics
import de.tillhub.printengine.data.PrintCommand
import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterResult
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.data.RawPrinterData
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.shouldBe
import io.mockk.Ordering
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

@RobolectricTest
internal class PrinterImplTest : DescribeSpec({

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
            every { observePrinterState() } returns MutableStateFlow(PrinterState.Connected)
            every { setFontSize(any()) } just Runs
            every { setIntensity(any()) } just Runs
            every { printText(any()) } just Runs
            every { printImage(any()) } just Runs
            every { printBarcode(any()) } just Runs
            every { printQr(any()) } just Runs
            coEvery { sendRawData(any()) } just Runs
            every { feedPaper() } just Runs
            every { cutPaper() } just Runs
            coEvery { start() } just Runs
            coEvery { getPrinterInfo() } returns PrinterInfo(
                serialNumber = "n/a",
                deviceModel = "P9 pro",
                printerVersion = "n/a",
                printerPaperSpec = PrintingPaperSpec.SunmiPaper56mm,
                printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
                printerHead = "n/a",
                printedDistance = 0,
                serviceVersion = PrinterServiceVersion.Unknown
            )
        }
        service = mockk {
            every { printController } returns controller
            every { printerState } returns MutableStateFlow(PrinterState.Connected)
        }
        analytics = mockk {
            every { logPrintReceipt(any()) } just Runs
            every { logErrorPrintReceipt(any()) } just Runs
        }
        printer = PrinterImpl(service, analytics)
    }

    afterSpec {
        bitmap.recycle()
    }

    describe("independent") {

        it("observePrinterState") {
            printer.printerState.first() shouldBe PrinterState.Connected
        }

        it("getPrinterInfo") {
            printer.getPrinterInfo() shouldBe PrinterResult.Success(
                PrinterInfo(
                    serialNumber = "n/a",
                    deviceModel = "P9 pro",
                    printerVersion = "n/a",
                    printerPaperSpec = PrintingPaperSpec.SunmiPaper56mm,
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
            printer.settings.enabled = true
        }

        it("printText") {
            printer.startPrintJob(
                PrintJob(
                    listOf(
                        PrintCommand.Text("text_to_print"),
                        PrintCommand.FeedPaper
                    )
                )
            )

            coVerify(ordering = Ordering.ORDERED) {
                controller.setIntensity(PrintingIntensity.DEFAULT)
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printText("text_to_print")
                controller.feedPaper()
                controller.start()
                analytics.logPrintReceipt(
                    "text_to_print\n" +
                            "-----FEED PAPER-----"
                )
            }
        }

        it("printText with DARK Intensity") {
            printer.settings.printingIntensity = PrintingIntensity.DARK
            printer.startPrintJob(
                PrintJob(
                    listOf(
                        PrintCommand.Text("text_to_print"),
                        PrintCommand.FeedPaper
                    )
                )
            )

            coVerify(ordering = Ordering.ORDERED) {
                controller.setIntensity(PrintingIntensity.DARK)
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printText("text_to_print")
                controller.feedPaper()
                controller.start()
            }
        }

        it("printReceipt with image") {
            printer.startPrintJob(
                PrintJob(
                    listOf(
                        PrintCommand.Image(bitmap),
                        PrintCommand.Text("receipt_to_print")
                    )
                )
            )

            coVerify(ordering = Ordering.ORDERED) {
                controller.setIntensity(PrintingIntensity.DEFAULT)
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printImage(bitmap)
                controller.printText("receipt_to_print")
                controller.start()
                analytics.logPrintReceipt(
                    "======IMAGE========\n" +
                            "receipt_to_print"
                )
            }
        }

        it("printReceipt") {
            printer.startPrintJob(
                PrintJob(
                    listOf(
                        PrintCommand.Image(bitmap),
                        PrintCommand.Text("raw_receipt_text"),
                        PrintCommand.QrCode("signature_qr_code"),
                        PrintCommand.Barcode("barcode"),
                        PrintCommand.Image(footerBitmap),
                        PrintCommand.FeedPaper
                    )
                )
            )

            coVerify(ordering = Ordering.ORDERED) {
                controller.setIntensity(PrintingIntensity.DEFAULT)
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printImage(bitmap)
                controller.printText("raw_receipt_text")
                controller.printQr("signature_qr_code")
                controller.printBarcode("barcode")
                controller.printImage(footerBitmap)
                controller.feedPaper()
                controller.start()
                analytics.logPrintReceipt(
                    "======IMAGE========\n" +
                            "raw_receipt_text\n" +
                            "==QR: signature_qr_code ==\n" +
                            "==BC: barcode ==\n" +
                            "======IMAGE========\n" +
                            "-----FEED PAPER-----"
                )
            }
        }

        it("print RawReceipt") {
            val rawPrinterData = RawPrinterData("raw_data".toByteArray())
            printer.startPrintJob(
                PrintJob(
                    listOf(
                        PrintCommand.RawData(rawPrinterData)
                    )
                )
            )

            coVerify(ordering = Ordering.ORDERED) {
                controller.setIntensity(PrintingIntensity.DEFAULT)
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.sendRawData(rawPrinterData)
                controller.start()
                analytics.logPrintReceipt("raw_data")
            }
        }
    }

    describe("printer disabled") {
        beforeTest {
            printer.settings.enabled = false
        }

        it("printText") {
            printer.startPrintJob(
                PrintJob(
                    listOf(
                        PrintCommand.Text("text_to_print"),
                        PrintCommand.FeedPaper
                    )
                )
            )

            verify(inverse = true) {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printText(any())
            }
        }

        it("printReceipt without image") {
//            printer.printReceipt("receipt_to_print", null)

            verify(inverse = true) {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printText("receipt_to_print")
            }

            verify(inverse = true) {
                controller.printImage(any())
            }
        }

        it("printReceipt") {
            printer.startPrintJob(
                PrintJob(
                    listOf(
                        PrintCommand.Image(bitmap),
                        PrintCommand.Text("raw_receipt_text"),
                        PrintCommand.QrCode("signature_qr_code"),
                        PrintCommand.Barcode("barcode"),
                        PrintCommand.Image(footerBitmap),
                        PrintCommand.FeedPaper
                    )
                )
            )

            verify(inverse = true) {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printImage(any())
                controller.printText(any())
                controller.printQr(any())
                controller.printBarcode(any())
                controller.printImage(any())
                controller.feedPaper()
            }
        }

        it("print RawReceipt") {
            printer.startPrintJob(
                PrintJob(
                    listOf(
                        PrintCommand.RawData(RawPrinterData("raw_data".toByteArray())),
                        PrintCommand.FeedPaper
                    )
                )
            )

            coVerify(inverse = true) {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.sendRawData(any())
            }
        }
    }
})
