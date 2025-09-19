package de.tillhub.printengine.star

import android.content.Context
import com.starmicronics.stario10.InterfaceType
import com.starmicronics.stario10.StarConnectionSettings
import com.starmicronics.stario10.StarDeviceDiscoveryManager
import com.starmicronics.stario10.StarDeviceDiscoveryManagerFactory
import com.starmicronics.stario10.StarIO10Exception
import com.starmicronics.stario10.StarPrinter
import com.starmicronics.stario10.StarPrinterInformation
import de.tillhub.printengine.data.ConnectionType
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.PrintingPaperSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList

class StarPrinterDiscoveryTest : FunSpec({

    lateinit var context: Context
    lateinit var discoveryManager: StarDeviceDiscoveryManager
    lateinit var starPrinter: StarPrinter
    lateinit var starPrinterDiscovery: StarPrinterDiscovery

    beforeEach {
        context = mockk()
        discoveryManager = mockk(relaxed = true)
        starPrinter = mockk()
        starPrinterDiscovery = StarPrinterDiscovery(context)

        mockkObject(StarDeviceDiscoveryManagerFactory)
        every {
            StarDeviceDiscoveryManagerFactory.create(
                listOf(InterfaceType.Lan, InterfaceType.Bluetooth, InterfaceType.Usb),
                context
            )
        } returns discoveryManager
    }

    afterEach {
        unmockkObject(StarDeviceDiscoveryManagerFactory)
    }

    test("observePrinters emits Discovering and Finished states on successful discovery") {
        val connectionSettings = mockk<StarConnectionSettings> {
            every { identifier } returns "SN123"
            every { interfaceType } returns InterfaceType.Lan
        }
        val printerInfo = mockk<StarPrinterInformation> {
            every { model } returns mockk { every { name } returns "StarModel" }
        }
        every { starPrinter.connectionSettings } returns connectionSettings
        every { starPrinter.information } returns printerInfo

        var capturedCallback: StarDeviceDiscoveryManager.Callback? = null

        every { discoveryManager.callback = any() } answers {
            capturedCallback = it.invocation.args[0] as? StarDeviceDiscoveryManager.Callback
        }

        every { discoveryManager.startDiscovery() } answers {
            capturedCallback?.onPrinterFound(starPrinter)
            capturedCallback?.onDiscoveryFinished()
        }

        val result = starPrinterDiscovery.observePrinters.take(3).toList()

        result shouldHaveSize 3
        result[0] shouldBe DiscoveryState.Idle

        val discoveringState = result[1].shouldBeTypeOf<DiscoveryState.Discovering>()
        discoveringState.printers shouldHaveSize 1
        with(discoveringState.printers[0]) {
            info.deviceModel shouldBe "StarModel"
            info.printerPaperSpec.shouldBeTypeOf<PrintingPaperSpec.External>().characterCount shouldBe 52
            manufacturer shouldBe "STAR"
            connectionAddress shouldBe "SN123"
            connectionType shouldBe ConnectionType.LAN
        }

        val finishedState = result[2].shouldBeTypeOf<DiscoveryState.Finished>()
        finishedState.printers shouldHaveSize 1
        finishedState.printers[0].connectionAddress shouldBe "SN123"
    }

    test("observePrinters emits Error state when discovery fails") {
        var capturedCallback: StarDeviceDiscoveryManager.Callback? = null

        every { discoveryManager.startDiscovery() } throws StarIO10Exception("Discovery failed")

        every { discoveryManager.callback = any() } answers {
            capturedCallback = it.invocation.args[0] as? StarDeviceDiscoveryManager.Callback
        }

        val result = starPrinterDiscovery.observePrinters.take(2).toList()

        capturedCallback shouldBe null
        result shouldHaveSize 2
        result[0] shouldBe DiscoveryState.Idle
        result[1].shouldBeTypeOf<DiscoveryState.Error>().message shouldBe "Discovery failed"

        verify { discoveryManager.stopDiscovery() }
    }

    test("observePrinters uses correct interface types") {
        var capturedCallback: StarDeviceDiscoveryManager.Callback? = null

        every { discoveryManager.callback = any() } answers {
            capturedCallback = it.invocation.args[0] as? StarDeviceDiscoveryManager.Callback
        }
        every { discoveryManager.startDiscovery() } answers {
            capturedCallback?.onDiscoveryFinished()
        }

        val result = starPrinterDiscovery.observePrinters.take(2).toList()

        result shouldHaveSize 2
        result[0] shouldBe DiscoveryState.Idle
        result[1].shouldBeTypeOf<DiscoveryState.Finished>().printers.shouldBeEmpty()

        verify {
            StarDeviceDiscoveryManagerFactory.create(
                listOf(InterfaceType.Lan, InterfaceType.Bluetooth, InterfaceType.Usb),
                context
            )
            discoveryManager.startDiscovery()
        }
    }

    test("observePrinters maps multiple printers correctly") {
        val printerInfo = mockk<StarPrinterInformation> {
            every { model } returns mockk { every { name } returns "StarModel" }
        }

        val printer1 = mockk<StarPrinter> {
            every { connectionSettings } returns mockk {
                every { identifier } returns "SN123"
                every { interfaceType } returns InterfaceType.Lan
            }
            every { information } returns printerInfo
        }

        val printer2 = mockk<StarPrinter> {
            every { connectionSettings } returns mockk {
                every { identifier } returns "BT456"
                every { interfaceType } returns InterfaceType.Bluetooth
            }
            every { information } returns printerInfo
        }

        var capturedCallback: StarDeviceDiscoveryManager.Callback? = null

        every { discoveryManager.callback = any() } answers {
            capturedCallback = it.invocation.args[0] as? StarDeviceDiscoveryManager.Callback
        }
        every { discoveryManager.startDiscovery() } answers {
            capturedCallback?.onPrinterFound(printer1)
            capturedCallback?.onPrinterFound(printer2)
            capturedCallback?.onDiscoveryFinished()
        }

        val result = starPrinterDiscovery.observePrinters.take(4).toList()

        result shouldHaveSize 4
        result[0] shouldBe DiscoveryState.Idle

        val discovering1 = result[1].shouldBeTypeOf<DiscoveryState.Discovering>()
        discovering1.printers shouldHaveSize 1
        discovering1.printers[0].connectionAddress shouldBe "SN123"
        discovering1.printers[0].connectionType shouldBe ConnectionType.LAN

        val discovering2 = result[2].shouldBeTypeOf<DiscoveryState.Discovering>()
        discovering2.printers shouldHaveSize 2
        discovering2.printers[1].connectionAddress shouldBe "BT456"
        discovering2.printers[1].connectionType shouldBe ConnectionType.BLUETOOTH

        val finished = result[3].shouldBeTypeOf<DiscoveryState.Finished>()
        finished.printers.map { it.connectionAddress } shouldBe listOf("SN123", "BT456")
    }
})
