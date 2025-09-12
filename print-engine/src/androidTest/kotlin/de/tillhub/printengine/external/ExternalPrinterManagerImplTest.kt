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
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@ExperimentalCoroutinesApi
class ExternalPrinterManagerImplTest : FunSpec({

    lateinit var context: Context
    lateinit var testDispatcher: TestDispatcher
    lateinit var dispatcherProvider: DispatcherProvider
    lateinit var epsonPrinterDiscovery: PrinterDiscovery
    lateinit var starPrinterDiscovery: PrinterDiscovery
    lateinit var manager: ExternalPrinterManager

    beforeTest {
        context = mockk()
        testDispatcher = UnconfinedTestDispatcher()
        dispatcherProvider = mockk {
            every { iO() } returns testDispatcher
        }
        epsonPrinterDiscovery = mockk()
        starPrinterDiscovery = mockk()
        manager = ExternalPrinterManagerImpl()
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

        coEvery { epsonPrinterDiscovery.observePrinters } returns flow {
            emit(DiscoveryState.Discovering(listOf(printer1)))
        }

        coEvery { starPrinterDiscovery.observePrinters } returns flow {
            emit(DiscoveryState.Discovering(listOf(printer2, printer3)))
            emit(DiscoveryState.Finished(listOf(printer2, printer3)))
        }

        val result =
            manager.discoverExternalPrinters(epsonPrinterDiscovery, starPrinterDiscovery).take(3)
                .toList()

        result[0].shouldBeTypeOf<DiscoveryState.Discovering>().printers shouldBe listOf(printer1)
        result[1].shouldBeTypeOf<DiscoveryState.Discovering>().printers shouldBe listOf(
            printer3,
            printer2
        )
        result[2].shouldBeTypeOf<DiscoveryState.Finished>().printers shouldBe listOf(
            printer3,
            printer2
        )
    }

    test("discoverExternalPrinters handles single discovery correctly") {
        val printer1 = mockk<ExternalPrinter> {
            every { connectionAddress } returns "SN001"
        }

        coEvery { epsonPrinterDiscovery.observePrinters } returns flow {
            emit(DiscoveryState.Discovering(listOf(printer1)))
            emit(DiscoveryState.Finished(listOf(printer1)))
        }

        val result = manager.discoverExternalPrinters(epsonPrinterDiscovery).take(2)
            .toList()

        result[0].shouldBeTypeOf<DiscoveryState.Discovering>().printers shouldBe listOf(printer1)
        result[1].shouldBeTypeOf<DiscoveryState.Finished>().printers shouldBe listOf(printer1)
    }

    test("discoverExternalPrinters emits Error when all discoveries fail without printers") {
        coEvery { epsonPrinterDiscovery.observePrinters } returns flow {
            emit(DiscoveryState.Error("Discovery 1 failed"))
        }

        coEvery { starPrinterDiscovery.observePrinters } returns flow {
            emit(DiscoveryState.Error("Discovery 2 failed"))
        }

        val result = manager.discoverExternalPrinters(epsonPrinterDiscovery, starPrinterDiscovery)
            .take(1).toList()

        result[0] shouldBe DiscoveryState.Error("Discovery 1 failed")
    }

    test("discoverExternalPrinters emits Discovered when at least one discovery succeeds") {
        val printer1 = mockk<ExternalPrinter> {
            every { connectionAddress } returns "SN001"
        }

        coEvery { epsonPrinterDiscovery.observePrinters } returns flow {
            emit(DiscoveryState.Discovering(listOf(printer1)))
            emit(DiscoveryState.Finished(listOf(printer1)))
        }

        coEvery { starPrinterDiscovery.observePrinters } returns flow {
            emit(DiscoveryState.Error("Discovery 2 failed"))
        }

        val result = manager.discoverExternalPrinters(epsonPrinterDiscovery, starPrinterDiscovery)
            .toList()

        result[0].shouldBeTypeOf<DiscoveryState.Discovering>().printers shouldBe listOf(printer1)
        result[1].shouldBeTypeOf<DiscoveryState.Finished>().printers shouldBe listOf(printer1)
    }

    test("discoverExternalPrinters emits Discovered with empty list when no discoveries provided") {
        val result = manager.discoverExternalPrinters()
            .toList()
        result shouldBe emptyList()
    }
})