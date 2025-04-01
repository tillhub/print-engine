package de.tillhub.printengine.external

import de.tillhub.printengine.Printer
import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterResult
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterSettings
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingPaperSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class ExternalPrinterContainerTest : FunSpec({

    lateinit var testDispatcher: TestDispatcher
    lateinit var container: ExternalPrinterContainer
    lateinit var printer: Printer

    beforeTest {
        printer = mockk {
            every { settings } returns PrinterSettings()
            every { observePrinterState() } returns MutableStateFlow(PrinterState.CheckingForPrinter)
            coEvery { getPrinterInfo() } returns PrinterResult.Success(PRINTER_INFO)
            coEvery { startPrintJob(any()) } returns PrinterResult.Success(Unit)
        }
        testDispatcher = UnconfinedTestDispatcher()
        container = ExternalPrinterContainer(TestScope(testDispatcher))
    }

    test("initial state should be CheckingForPrinter") {
        val initialState = container.observePrinterState().first()
        initialState shouldBe PrinterState.Error.NotAvailable
    }

    test("observePrinterState emits new printer's state after initPrinter") {
        runTest(testDispatcher) {
            every { printer.observePrinterState() } returns MutableStateFlow(PrinterState.Connected)

            container.initPrinter(printer)

            advanceUntilIdle()

            val state = container.observePrinterState().first()
            state shouldBe PrinterState.Connected
        }
    }

    test("observePrinterState updates when new printer's state changes") {
        runTest(testDispatcher) {
            val newPrinterState = MutableStateFlow<PrinterState>(PrinterState.CheckingForPrinter)
            every { printer.observePrinterState() } returns newPrinterState

            container.initPrinter(printer)
            advanceUntilIdle()

            container.observePrinterState().first() shouldBe PrinterState.CheckingForPrinter

            newPrinterState.value = PrinterState.Connected
            advanceUntilIdle()

            container.observePrinterState().first() shouldBe PrinterState.Connected
        }
    }

    test("settings delegates to selectedPrinter") {
        val settings = container.settings
        settings shouldBe printer.settings
    }

    test("getPrinterInfo delegates to selectedPrinter") {
        val result = container.getPrinterInfo()
        result.shouldBeInstanceOf<PrinterResult.Success<PrinterInfo>>()
        result.value shouldBe PRINTER_INFO
    }

    test("startPrintJob delegates to selectedPrinter") {
        val printJob = mockk<PrintJob> {
            every { description } returns "test"
        }
        container.initPrinter(printer)
        container.startPrintJob(printJob)
        coVerify {
            printer.startPrintJob(printJob)
        }
    }
}) {
    companion object {
        private val PRINTER_INFO = PrinterInfo(
            serialNumber = "n/a",
            deviceModel = "Emulated Printer",
            printerVersion = "n/a",
            printerPaperSpec = PrintingPaperSpec.PaxPaper56mm,
            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
            printerHead = "n/a",
            printedDistance = 0,
            serviceVersion = PrinterServiceVersion.Info(
                serviceVersionName = "1.0.0",
                serviceVersionCode = 1
            )
        )
    }
}