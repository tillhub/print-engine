package de.tillhub.printengine.external

import android.content.Context
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.dispatcher.DispatcherProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@ExperimentalCoroutinesApi
class ExternalPrinterManagerImplTest : FunSpec({

    lateinit var context: Context
    lateinit var testDispatcher: TestDispatcher
    lateinit var dispatcherProvider: DispatcherProvider
    lateinit var printerDiscovery1: PrinterDiscovery
    lateinit var printerDiscovery2: PrinterDiscovery
    lateinit var manager: ExternalPrinterManager

    beforeTest {
        context = mockk()
        testDispatcher = UnconfinedTestDispatcher()
        dispatcherProvider = mockk {
            every { iO() } returns testDispatcher
        }
        printerDiscovery1 = mockk()
        printerDiscovery2 = mockk()
        manager = ExternalPrinterManagerImpl(context, dispatcherProvider)
    }

    test("discoverExternalPrinters emits Idle then Discovered with unique printers") {
        val printer1 = mockk<ExternalPrinter> {
            every { connectionAddress } returns "SN001"
        }
        val printer2 = mockk<ExternalPrinter> {
            every { connectionAddress } returns "SN002"
        }
        val printer3 = mockk<ExternalPrinter> {
            every { connectionAddress } returns "SN001"
        }

        coEvery { printerDiscovery1.discoverPrinter(context) } returns flow {
            emit(DiscoveryState.Discovering(listOf(printer1)))
            emit(DiscoveryState.Finished(listOf(printer1)))
        }

        coEvery { printerDiscovery2.discoverPrinter(context) } returns flow {
            emit(DiscoveryState.Discovering(listOf(printer2, printer3)))
            emit(DiscoveryState.Finished(listOf(printer2, printer3)))
        }

        val result = manager.discoverExternalPrinters(printerDiscovery1, printerDiscovery2)
            .toList()

        result[0] shouldBe DiscoveryState.Idle
        result[1].shouldBeTypeOf<DiscoveryState.Discovering>().printers shouldBe listOf(printer1)
        result[2].shouldBeTypeOf<DiscoveryState.Discovering>().printers shouldBe listOf(printer1, printer2)
        result[3].shouldBeTypeOf<DiscoveryState.Finished>().printers shouldBe listOf(printer1, printer2)
    }

    test("discoverExternalPrinters handles single discovery correctly") {
        val printer1 = mockk<ExternalPrinter> {
            every { connectionAddress } returns "SN001"
        }

        coEvery { printerDiscovery1.discoverPrinter(context) } returns flow {
            emit(DiscoveryState.Discovering(listOf(printer1)))
            emit(DiscoveryState.Finished(listOf(printer1)))
        }

        val result = manager.discoverExternalPrinters(printerDiscovery1)
            .toList()

        result[0] shouldBe DiscoveryState.Idle
        result[1].shouldBeTypeOf<DiscoveryState.Discovering>().printers shouldBe listOf(printer1)
        result[2].shouldBeTypeOf<DiscoveryState.Finished>().printers shouldBe listOf(printer1)
    }

    test("discoverExternalPrinters ignores Error and Idle states from discoveries") {
        val printer1 = mockk<ExternalPrinter> {
            every { connectionAddress } returns "SN001"
        }

        coEvery { printerDiscovery1.discoverPrinter(context) } returns flow {
            emit(DiscoveryState.Discovering(listOf(printer1)))
            emit(DiscoveryState.Error("Some error"))
            emit(DiscoveryState.Idle)
            emit(DiscoveryState.Finished(listOf(printer1)))
        }

        val result = manager.discoverExternalPrinters(printerDiscovery1)
            .toList()

        result[0] shouldBe DiscoveryState.Idle
        result[1].shouldBeTypeOf<DiscoveryState.Discovering>().printers shouldBe listOf(printer1)
        result[2].shouldBeTypeOf<DiscoveryState.Finished>().printers shouldBe listOf(printer1)
    }
})