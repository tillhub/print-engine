package de.tillhub.printengine

import de.tillhub.printengine.analytics.PrintAnalytics
import de.tillhub.printengine.barcode.BarcodeEncoderImpl
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.external.ExternalPrinterManagerImpl
import de.tillhub.printengine.external.PrinterDiscovery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList

class PrintEngineTest : FunSpec({

    lateinit var printEngine: PrintEngine

    beforeTest {
        mockkConstructor(ExternalPrinterManagerImpl::class)
        mockkConstructor(PrinterContainer::class)
        mockkConstructor(BarcodeEncoderImpl::class)
        printEngine = PrintEngine.Companion.getInstance()
    }

    afterTest {
        unmockkAll()
    }

    test("setAnalytics should set printAnalytics and return self") {
        val analytics = mockk<PrintAnalytics>()
        val engine = printEngine.setAnalytics(analytics)
        engine shouldBe printEngine
    }

    test("printer should return PrinterContainer instance") {
        val printer = printEngine.printer
        printer.shouldBeInstanceOf<PrinterContainer>()
    }

    test("barcodeEncoder should return BarcodeEncoderImpl instance") {
        val encoder = printEngine.barcodeEncoder
        encoder.shouldBeInstanceOf<BarcodeEncoderImpl>()
    }

    test("discoverExternalPrinters delegates to externalPrinterManager") {
        val printer = mockk<ExternalPrinter> {
            every { connectionAddress } returns "SN001"
        }

        every {
            anyConstructed<ExternalPrinterManagerImpl>().discoverExternalPrinters(*anyVararg())
        } returns flowOf(DiscoveryState.Finished(listOf(printer)))

        val discovery1 = mockk<PrinterDiscovery>(relaxed = true)
        val flow = printEngine.discoverExternalPrinters(discovery1)

        val result = flow.toList().first()
        result.shouldBeInstanceOf<DiscoveryState.Finished>()
    }

    test("initPrinter should initialize printer and return success") {
        val mockPrintService = mockk<PrintService>()
        val mockAnalytics = mockk<PrintAnalytics>()

        // Set analytics
        printEngine.setAnalytics(mockAnalytics)
        val printerContainer = printEngine.printer as PrinterContainer

        // Mock PrinterImpl constructor and its instance
        val mockPrinterImpl = mockk<PrinterImpl>()
        every { anyConstructed<PrinterImpl>().initialize() } just Runs // If initialize is called
        every { anyConstructed<PrinterImpl>() } returns mockPrinterImpl // Return mock instance

        // Mock PrinterContainer's initPrinter method
        every { printerContainer.initPrinter(mockPrinterImpl) } just Runs

        // Call the method under test
        val result = printEngine.initPrinter(mockPrintService)

        // Assertions
        result.shouldBeInstanceOf<Printer>()
        result shouldBe printEngine.printer

        // Verify interactions
        verify { printerContainer.initPrinter(mockPrinterImpl) }
    }
})
