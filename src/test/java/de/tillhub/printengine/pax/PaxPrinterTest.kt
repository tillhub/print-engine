package de.tillhub.printengine.pax

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
class PaxPrinterTest : DescribeSpec({

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
            every { setIntensity(any()) } just Runs
            every { start() } just Runs
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
        }
        analytics = mockk {
            every { logPrintReceipt(any()) } just Runs
            every { logErrorPrintReceipt(any()) } just Runs
        }
        printer = PaxPrinter(service, analytics)
    }

    afterSpec {
        bitmap.recycle()
    }

    describe("independent") {

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
            printer.setEnabled(true)
        }

        describe("default intensity") {
            it("printText") {
                printer.startPrintJob(
                    PrintJob(
                        listOf(
                            PrintCommand.Text("text_to_print"),
                            PrintCommand.FeedPaper
                        )
                    )
                )

                verify {
                    controller.setIntensity(PrintingIntensity.DEFAULT)
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
                            PrintCommand.Text("receipt_to_print"),
                            PrintCommand.FeedPaper
                        )
                    )
                )

                verify {
                    controller.setIntensity(PrintingIntensity.DEFAULT)
                    controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                    controller.printImage(bitmap)
                    controller.printText("receipt_to_print")
                    controller.feedPaper()
                    controller.start()
                    analytics.logPrintReceipt("======IMAGE========\n" +
                            "receipt_to_print\n" +
                            "-----FEED PAPER-----")
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

                verify {
                    controller.setIntensity(PrintingIntensity.DEFAULT)
                    controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                    controller.printImage(bitmap)
                    controller.printText("raw_receipt_text")
                    controller.printQr("signature_qr_code")
                    controller.printBarcode("barcode")
                    controller.printImage(footerBitmap)
                    controller.feedPaper()
                    controller.start()
                    analytics.logPrintReceipt("======IMAGE========\n" +
                            "raw_receipt_text\n" +
                            "==QR: signature_qr_code ==\n" +
                            "==BC: barcode ==\n" +
                            "======IMAGE========\n" +
                            "-----FEED PAPER-----")
                }
            }

            it("print RawReceipt") {
                val rawPrinterData = RawPrinterData("raw_data".toByteArray())
                printer.startPrintJob(
                    PrintJob(
                        listOf(
                            PrintCommand.RawData(rawPrinterData),
                            PrintCommand.FeedPaper
                        )
                    )
                )

                verify {
                    controller.setIntensity(PrintingIntensity.DEFAULT)
                    controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                    controller.sendRawData(rawPrinterData)
                    controller.feedPaper()
                    controller.start()
                    analytics.logPrintReceipt("raw_data\n" +
                            "-----FEED PAPER-----")
                }
            }
        }

        describe("changed intensity") {
            beforeTest {
                printer.setPrintingIntensity(PrintingIntensity.DARK)
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

                verify {
                    controller.setIntensity(PrintingIntensity.DARK)
                    controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                    controller.printText("text_to_print")
                    controller.feedPaper()
                    controller.start()
                }
            }

            it("printReceipt without image") {
                printer.startPrintJob(
                    PrintJob(
                        listOf(
                            PrintCommand.Text("receipt_to_print"),
                            PrintCommand.FeedPaper
                        )
                    )
                )

                verify {
                    controller.setIntensity(PrintingIntensity.DARK)
                    controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                    controller.printText("receipt_to_print")
                    controller.feedPaper()
                    controller.start()
                    analytics.logPrintReceipt("receipt_to_print\n" +
                            "-----FEED PAPER-----")
                }

                verify(inverse = true) {
                    controller.printImage(any())
                }
            }

            it("printReceipt with image") {
                printer.startPrintJob(
                    PrintJob(
                        listOf(
                            PrintCommand.Image(bitmap),
                            PrintCommand.Text("receipt_to_print"),
                            PrintCommand.FeedPaper
                        )
                    )
                )

                verify {
                    controller.setIntensity(PrintingIntensity.DARK)
                    controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                    controller.printImage(bitmap)
                    controller.printText("receipt_to_print")
                    controller.feedPaper()
                    controller.start()
                    analytics.logPrintReceipt("======IMAGE========\n" +
                            "receipt_to_print\n" +
                            "-----FEED PAPER-----")
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

                verify {
                    controller.setIntensity(PrintingIntensity.DARK)
                    controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                    controller.printImage(bitmap)
                    controller.printText("raw_receipt_text")
                    controller.printQr("signature_qr_code")
                    controller.printBarcode("barcode")
                    controller.printImage(footerBitmap)
                    controller.feedPaper()
                    controller.start()
                    analytics.logPrintReceipt("======IMAGE========\n" +
                            "raw_receipt_text\n" +
                            "==QR: signature_qr_code ==\n" +
                            "==BC: barcode ==\n" +
                            "======IMAGE========\n" +
                            "-----FEED PAPER-----")
                }
            }

            it("print RawReceipt") {
                val rawPrinterData = RawPrinterData("raw_data".toByteArray())
                printer.startPrintJob(
                    PrintJob(
                        listOf(
                            PrintCommand.RawData(rawPrinterData),
                            PrintCommand.FeedPaper
                        )
                    )
                )

                verify {
                    controller.setIntensity(PrintingIntensity.DARK)
                    controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                    controller.sendRawData(rawPrinterData)
                    controller.feedPaper()
                    controller.start()
                    analytics.logPrintReceipt("raw_data\n" +
                            "-----FEED PAPER-----")
                }
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
            printer.setEnabled(false)
        }

        it("printText") {
            printer.startPrintJob(
                PrintJob(
                    listOf(
                        PrintCommand.Text("receipt_to_print"),
                        PrintCommand.FeedPaper
                    )
                )
            )

            verify(inverse = true) {
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
                        PrintCommand.Text("receipt_to_print"),
                        PrintCommand.FeedPaper
                    )
                )
            )

            verify(inverse = true) {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.printImage(bitmap)
                controller.printText("receipt_to_print")
                controller.feedPaper()
                controller.start()
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
                controller.printImage(bitmap)
                controller.printText(any())
                controller.printQr(any())
                controller.printBarcode(any())
                controller.printImage(any())
                controller.feedPaper()
                controller.start()
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

            verify(inverse = true) {
                controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
                controller.sendRawData(any())
                controller.feedPaper()
                controller.start()
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
