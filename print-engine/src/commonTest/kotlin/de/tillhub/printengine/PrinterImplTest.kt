package de.tillhub.printengine

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
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PrinterImplTest {
    lateinit var controller: PrinterController
    lateinit var service: PrintService
    lateinit var analytics: PrintAnalytics
    lateinit var printer: Printer

    @BeforeTest
    fun setup() {
        controller =
            mock {
                every { observePrinterState() } returns MutableStateFlow(PrinterState.Connected)
                every { setFontSize(any()) } returns Unit
                every { setIntensity(any()) } returns Unit
                every { printText(any()) } returns Unit
                every { printBarcode(any()) } returns Unit
                every { printQr(any()) } returns Unit
                everySuspend { sendRawData(any()) } returns Unit
                every { feedPaper() } returns Unit
                every { cutPaper() } returns Unit
                everySuspend { start() } returns Unit
                everySuspend { getPrinterInfo() } returns
                    PrinterInfo(
                        serialNumber = "n/a",
                        deviceModel = "P9 pro",
                        printerVersion = "n/a",
                        printerPaperSpec = PrintingPaperSpec.SunmiPaper56mm,
                        printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
                        printerHead = "n/a",
                        printedDistance = 0,
                        serviceVersion = PrinterServiceVersion.Unknown,
                    )
            }
        service =
            mock {
                every { printController } returns controller
                every { printerState } returns MutableStateFlow(PrinterState.Connected)
            }
        analytics =
            mock {
                every { logPrintReceipt(any()) } returns Unit
                every { logErrorPrintReceipt(any()) } returns Unit
            }
        printer = PrinterImpl(service, analytics)
    }

    @Test
    fun observePrinterState() = runTest {
        assertEquals(PrinterState.Connected, printer.printerState.first())
    }

    @Test
    fun getPrinterInfo() = runTest {
        val expected =
            PrinterResult.Success(
                PrinterInfo(
                    serialNumber = "n/a",
                    deviceModel = "P9 pro",
                    printerVersion = "n/a",
                    printerPaperSpec = PrintingPaperSpec.SunmiPaper56mm,
                    printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
                    printerHead = "n/a",
                    printedDistance = 0,
                    serviceVersion = PrinterServiceVersion.Unknown,
                ),
            )
        assertEquals(expected, printer.getPrinterInfo())
    }

    @Test
    fun `printer enabled printText`() = runTest {
        printer.settings.enabled = true

        printer.startPrintJob(
            PrintJob(
                listOf(
                    PrintCommand.Text("text_to_print"),
                    PrintCommand.FeedPaper,
                ),
            ),
        )

        verifySuspend(mode = VerifyMode.order) {
            controller.setIntensity(PrintingIntensity.DEFAULT)
            controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
            controller.printText("text_to_print")
            controller.feedPaper()
            controller.start()
            analytics.logPrintReceipt(
                "text_to_print\n" +
                    "-----FEED PAPER-----",
            )
        }
    }

    @Test
    fun `printer enabled printText with DARK Intensity`() = runTest {
        printer.settings.enabled = true

        printer.settings.printingIntensity = PrintingIntensity.DARK
        printer.startPrintJob(
            PrintJob(
                listOf(
                    PrintCommand.Text("text_to_print"),
                    PrintCommand.FeedPaper,
                ),
            ),
        )

        verifySuspend(mode = VerifyMode.order) {
            controller.setIntensity(PrintingIntensity.DARK)
            controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
            controller.printText("text_to_print")
            controller.feedPaper()
            controller.start()
        }
    }

    @Test
    fun `printer enabled print RawReceipt`() = runTest {
        printer.settings.enabled = true

        val rawPrinterData = RawPrinterData("raw_data".encodeToByteArray())
        printer.startPrintJob(
            PrintJob(
                listOf(
                    PrintCommand.RawData(rawPrinterData),
                ),
            ),
        )

        verifySuspend(mode = VerifyMode.order) {
            controller.setIntensity(PrintingIntensity.DEFAULT)
            controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
            controller.sendRawData(rawPrinterData)
            controller.start()
            analytics.logPrintReceipt("raw_data")
        }
    }

    @Test
    fun `printer disabled printText`() = runTest {
        printer.settings.enabled = false

        printer.startPrintJob(
            PrintJob(
                listOf(
                    PrintCommand.Text("text_to_print"),
                    PrintCommand.FeedPaper,
                ),
            ),
        )

        verify(mode = VerifyMode.not) {
            controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
            controller.printText(any())
        }
    }

    @Test
    fun `printer disabled print RawReceipt`() = runTest {
        printer.settings.enabled = false

        printer.startPrintJob(
            PrintJob(
                listOf(
                    PrintCommand.RawData(RawPrinterData("raw_data".encodeToByteArray())),
                    PrintCommand.FeedPaper,
                ),
            ),
        )

        verifySuspend(mode = VerifyMode.not) {
            controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)
            controller.sendRawData(any())
        }
    }
}
