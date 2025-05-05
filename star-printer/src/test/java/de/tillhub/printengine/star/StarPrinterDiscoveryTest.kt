package de.tillhub.printengine.star

import android.content.Context
import com.starmicronics.stario10.InterfaceType
import com.starmicronics.stario10.StarConnectionSettings
import com.starmicronics.stario10.StarDeviceDiscoveryManager
import com.starmicronics.stario10.StarDeviceDiscoveryManagerFactory
import com.starmicronics.stario10.StarPrinter
import com.starmicronics.stario10.StarPrinterInformation
import de.tillhub.printengine.data.ConnectionType
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingPaperSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.toList

class StarPrinterDiscoveryTest : FunSpec({
    lateinit var context: Context
    lateinit var discoveryManager: StarDeviceDiscoveryManager
    lateinit var starPrinter: StarPrinter

    beforeEach {
        context = mockk()
        discoveryManager = mockk(relaxed = true)
        starPrinter = mockk()

        mockkObject(StarDeviceDiscoveryManagerFactory)
        every {
            StarDeviceDiscoveryManagerFactory.create(
                listOf(InterfaceType.Lan, InterfaceType.Bluetooth, InterfaceType.Usb),
                context
            )
        } returns discoveryManager

        every { discoveryManager.discoveryTime } returns 10000
    }

    afterEach {
        unmockkObject(StarDeviceDiscoveryManagerFactory)
    }

    test("discoverPrinter emits Discovering and Discovered states on successful discovery") {
        val connectionSettings = mockk<StarConnectionSettings> {
            every { identifier } returns "SN123"
            every { interfaceType } returns InterfaceType.Lan
        }
        val printerInfo = mockk<StarPrinterInformation> {
            every { model } returns mockk { every { name } returns "StarModel" }
        }
        every { starPrinter.connectionSettings } returns connectionSettings
        every { starPrinter.information } returns printerInfo

        val callbackSlot = slot<StarDeviceDiscoveryManager.Callback>()
        every { discoveryManager.callback = any() } answers {
            callbackSlot.captured = args[0] as StarDeviceDiscoveryManager.Callback
        }
        every { discoveryManager.startDiscovery() } answers {
            callbackSlot.captured.onPrinterFound(starPrinter)
            callbackSlot.captured.onDiscoveryFinished()
        }

        val result = StarPrinterDiscovery.discoverPrinter(context).toList()

        result.size shouldBe 3
        result[0] shouldBe DiscoveryState.Idle

        val discoveringState = result[1] as DiscoveryState.Discovering
        discoveringState.printers.size shouldBe 1
        with(discoveringState.printers[0]) {
            info.serialNumber shouldBe "n/a"
            info.deviceModel shouldBe "StarModel"
            info.printerVersion shouldBe "n/a"
            info.printerPaperSpec.shouldBeTypeOf<PrintingPaperSpec.External>().characterCount shouldBe 32
            info.printingFontType shouldBe PrintingFontType.DEFAULT_FONT_SIZE
            info.printerHead shouldBe "n/a"
            info.printedDistance shouldBe 0
            info.serviceVersion shouldBe PrinterServiceVersion.Unknown
            manufacturer shouldBe StarManufacturer
            connectionAddress shouldBe "SN123"
            connectionType shouldBe ConnectionType.LAN
        }

        val discoveredState = result[2] as DiscoveryState.Discovered
        discoveredState.printers.shouldBeEmpty()

        verify {
            discoveryManager.startDiscovery()
            discoveryManager.stopDiscovery()
        }
    }

    test("discoverPrinter emits Error state when discovery fails") {
        every { discoveryManager.startDiscovery() } throws RuntimeException("Discovery failed")

        val result = StarPrinterDiscovery.discoverPrinter(context).toList()

        result.size shouldBe 2
        result[0] shouldBe DiscoveryState.Idle
        result[1].shouldBeTypeOf<DiscoveryState.Error>().message shouldBe "Discovery failed"

        verify { discoveryManager.stopDiscovery() }
    }

    test("discoverPrinter uses correct interface types and timeout") {
        val callbackSlot = slot<StarDeviceDiscoveryManager.Callback>()
        every { discoveryManager.callback = any() } answers {
            callbackSlot.captured = args[0] as StarDeviceDiscoveryManager.Callback
        }
        every { discoveryManager.startDiscovery() } answers {
            callbackSlot.captured.onDiscoveryFinished()
        }

        val result = StarPrinterDiscovery.discoverPrinter(context).toList()

        result.size shouldBe 2
        result[0] shouldBe DiscoveryState.Idle
        result[1].shouldBeTypeOf<DiscoveryState.Discovered>().printers shouldBe emptyList()

        verify {
            StarDeviceDiscoveryManagerFactory.create(
                listOf(InterfaceType.Lan, InterfaceType.Bluetooth, InterfaceType.Usb),
                context
            )
            discoveryManager.discoveryTime = 10000
            discoveryManager.startDiscovery()
            discoveryManager.stopDiscovery()
        }
    }

    test("discoverPrinter maps multiple printers correctly") {
        val printer1Settings = mockk<StarConnectionSettings> {
            every { identifier } returns "SN123"
            every { interfaceType } returns InterfaceType.Lan
        }
        val printer2Settings = mockk<StarConnectionSettings> {
            every { identifier } returns "BT456"
            every { interfaceType } returns InterfaceType.Bluetooth
        }
        val printerInfo = mockk<StarPrinterInformation> {
            every { model } returns mockk { every { name } returns "StarModel" }
        }
        val printer1 = mockk<StarPrinter> {
            every { connectionSettings } returns printer1Settings
            every { information } returns printerInfo
        }
        val printer2 = mockk<StarPrinter> {
            every { connectionSettings } returns printer2Settings
            every { information } returns printerInfo
        }

        val callbackSlot = slot<StarDeviceDiscoveryManager.Callback>()
        every { discoveryManager.callback = any() } answers {
            callbackSlot.captured = args[0] as StarDeviceDiscoveryManager.Callback
        }
        every { discoveryManager.startDiscovery() } answers {
            callbackSlot.captured.onPrinterFound(printer1)
            callbackSlot.captured.onPrinterFound(printer2)
            callbackSlot.captured.onDiscoveryFinished()
        }

        val result = StarPrinterDiscovery.discoverPrinter(context).toList()

        result.size shouldBe 4
        result[0] shouldBe DiscoveryState.Idle

        val discovering1 = result[1] as DiscoveryState.Discovering
        discovering1.printers.size shouldBe 1
        discovering1.printers[0].connectionAddress shouldBe "SN123"
        discovering1.printers[0].connectionType shouldBe ConnectionType.LAN

        val discovering2 = result[2] as DiscoveryState.Discovering
        discovering2.printers.size shouldBe 1
        discovering2.printers[0].connectionAddress shouldBe "BT456"
        discovering2.printers[0].connectionType shouldBe ConnectionType.BLUETOOTH

        val discovered = result[3] as DiscoveryState.Discovered
        discovered.printers.shouldBeEmpty()
    }
})
