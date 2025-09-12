package de.tillhub.printengine.external

import de.tillhub.printengine.data.ConnectionType
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingPaperSpec
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ExternalPrinterManagerImplTest {

    private lateinit var epsonPrinterDiscovery: PrinterDiscovery
    private lateinit var starPrinterDiscovery: PrinterDiscovery
    private lateinit var manager: ExternalPrinterManager

    @BeforeTest
    fun setup() {
        epsonPrinterDiscovery = mock()
        starPrinterDiscovery = mock()
        manager = ExternalPrinterManagerImpl()
    }

    @Test
    fun `discoverExternalPrinters emits Idle then Discovered with unique printers`() = runTest {
        val printer1 = ExternalPrinter(
            connectionAddress = "SN001",
            info = PRINTER_INFO,
            connectionType = ConnectionType.BLUETOOTH,
            manufacturer = "Epson"
        )
        val printer2 = ExternalPrinter(
            connectionAddress = "SN002",
            info = PRINTER_INFO,
            connectionType = ConnectionType.BLUETOOTH,
            manufacturer = "Epson"
        )
        val printer3 = ExternalPrinter(
            connectionAddress = "SN001",
            info = PRINTER_INFO,
            connectionType = ConnectionType.BLUETOOTH,
            manufacturer = "Epson"
        )

        everySuspend { epsonPrinterDiscovery.observePrinters } returns flow {
            emit(DiscoveryState.Discovering(listOf(printer1)))
        }

        everySuspend { starPrinterDiscovery.observePrinters } returns flow {
            emit(DiscoveryState.Discovering(listOf(printer2, printer3)))
            emit(DiscoveryState.Finished(listOf(printer2, printer3)))
        }

        val result =
            manager.discoverExternalPrinters(epsonPrinterDiscovery, starPrinterDiscovery).take(3)
                .toList()

        assertTrue(result[0] is DiscoveryState.Discovering)
        assertEquals(listOf(printer1), (result[0] as DiscoveryState.Discovering).printers)
        
        assertTrue(result[1] is DiscoveryState.Discovering)
        assertEquals(listOf(printer3, printer2), (result[1] as DiscoveryState.Discovering).printers)
        
        assertTrue(result[2] is DiscoveryState.Finished)
        assertEquals(listOf(printer3, printer2), (result[2] as DiscoveryState.Finished).printers)
    }

    @Test
    fun `discoverExternalPrinters handles single discovery correctly`() = runTest {
        val printer1 = ExternalPrinter(
            connectionAddress = "SN001",
            info = PRINTER_INFO,
            connectionType = ConnectionType.BLUETOOTH,
            manufacturer = "Epson"
        )

        everySuspend { epsonPrinterDiscovery.observePrinters } returns flow {
            emit(DiscoveryState.Discovering(listOf(printer1)))
            emit(DiscoveryState.Finished(listOf(printer1)))
        }

        val result = manager.discoverExternalPrinters(epsonPrinterDiscovery).take(2)
            .toList()

        assertTrue(result[0] is DiscoveryState.Discovering)
        assertEquals(listOf(printer1), (result[0] as DiscoveryState.Discovering).printers)
        
        assertTrue(result[1] is DiscoveryState.Finished)
        assertEquals(listOf(printer1), (result[1] as DiscoveryState.Finished).printers)
    }

    @Test
    fun `discoverExternalPrinters emits Error when all discoveries fail without printers`() = runTest {
        everySuspend { epsonPrinterDiscovery.observePrinters } returns flow {
            emit(DiscoveryState.Error("Discovery 1 failed"))
        }

        everySuspend { starPrinterDiscovery.observePrinters } returns flow {
            emit(DiscoveryState.Error("Discovery 2 failed"))
        }

        val result = manager.discoverExternalPrinters(epsonPrinterDiscovery, starPrinterDiscovery)
            .take(1).toList()

        assertEquals(DiscoveryState.Error("Discovery 1 failed"), result[0])
    }

    @Test
    fun `discoverExternalPrinters emits Discovered when at least one discovery succeeds`() = runTest {
        val printer1 = ExternalPrinter(
            connectionAddress = "SN001",
            info = PRINTER_INFO,
            connectionType = ConnectionType.BLUETOOTH,
            manufacturer = "Epson"
        )

        everySuspend { epsonPrinterDiscovery.observePrinters } returns flow {
            emit(DiscoveryState.Discovering(listOf(printer1)))
            emit(DiscoveryState.Finished(listOf(printer1)))
        }

        everySuspend { starPrinterDiscovery.observePrinters } returns flow {
            emit(DiscoveryState.Error("Discovery 2 failed"))
        }

        val result = manager.discoverExternalPrinters(epsonPrinterDiscovery, starPrinterDiscovery)
            .toList()

        assertTrue(result[0] is DiscoveryState.Discovering)
        assertEquals(listOf(printer1), (result[0] as DiscoveryState.Discovering).printers)
        
        assertTrue(result[1] is DiscoveryState.Finished)
        assertEquals(listOf(printer1), (result[1] as DiscoveryState.Finished).printers)
    }

    @Test
    fun `discoverExternalPrinters emits Discovered with empty list when no discoveries provided`() = runTest {
        val result = manager.discoverExternalPrinters()
            .toList()
        assertEquals(emptyList(), result)
    }

    companion object {
        private val PRINTER_INFO = PrinterInfo(
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
}