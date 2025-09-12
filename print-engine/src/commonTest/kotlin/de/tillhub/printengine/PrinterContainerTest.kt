package de.tillhub.printengine

import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterResult
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterSettings
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingPaperSpec
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PrinterContainerTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var container: PrinterContainer
    private lateinit var printer: Printer

    @BeforeTest
    fun beforeTest() {
        printer = mock {
            every { settings } returns PrinterSettings()
            every { printerState } returns MutableStateFlow(PrinterState.CheckingForPrinter)
            everySuspend { getPrinterInfo() } returns PrinterResult.Success(PRINTER_INFO)
            everySuspend { startPrintJob(any()) } returns PrinterResult.Success(Unit)
        }
        testDispatcher = UnconfinedTestDispatcher()
        container = PrinterContainer()
    }

    @Test
    fun `initial state should be CheckingForPrinter`() = runTest(testDispatcher) {
        val initialState = container.printerState.first()
        assertEquals(PrinterState.Error.NotAvailable, initialState)
    }

    @Test
    fun `observePrinterState emits new printer's state after initPrinter`() = runTest(testDispatcher) {
        every { printer.printerState } returns MutableStateFlow(PrinterState.Connected)

        container.initPrinter(printer)

        advanceUntilIdle()

        val state = container.printerState.first()
        assertEquals(PrinterState.Connected, state)
    }

    @Test
    fun `observePrinterState updates when new printer's state changes`() = runTest(testDispatcher) {
        val newPrinterState = MutableStateFlow<PrinterState>(PrinterState.CheckingForPrinter)
        every { printer.printerState } returns newPrinterState

        container.initPrinter(printer)
        advanceUntilIdle()

        assertEquals(PrinterState.CheckingForPrinter, container.printerState.first())

        newPrinterState.value = PrinterState.Connected
        advanceUntilIdle()

        assertEquals(PrinterState.Connected, container.printerState.first())
    }

    @Test
    fun `settings delegates to selectedPrinter`() = runTest(testDispatcher) {
        val settings = container.settings
        assertEquals(printer.settings, settings)
    }

    @Test
    fun `getPrinterInfo delegates to selectedPrinter`() = runTest(testDispatcher) {
        val result = container.getPrinterInfo()
        assertTrue(result is PrinterResult.Success<PrinterInfo>)
        assertEquals(PRINTER_INFO, result.value)
    }

    @Test
    fun `startPrintJob delegates to selectedPrinter`() = runTest(testDispatcher) {
        val printJob = PrintJob(emptyList())
        container.initPrinter(printer)
        container.startPrintJob(printJob)
        verifySuspend {
            printer.startPrintJob(printJob)
        }
    }

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