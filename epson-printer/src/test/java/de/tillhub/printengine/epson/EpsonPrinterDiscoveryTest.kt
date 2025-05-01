package de.tillhub.printengine.epson


import android.content.Context
import com.epson.epos2.Epos2Exception
import com.epson.epos2.discovery.DeviceInfo
import com.epson.epos2.discovery.Discovery
import de.tillhub.printengine.data.ConnectionType
import de.tillhub.printengine.data.DiscoveryState
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList

@ExperimentalCoroutinesApi
class EpsonPrinterDiscoveryTest : FunSpec({

    lateinit var discoveryWrapper: DiscoveryWrapper
    lateinit var context: Context
    lateinit var printerDiscovery: EpsonPrinterDiscovery

    beforeTest {
        discoveryWrapper = mockk()
        context = mockk()
        printerDiscovery = EpsonPrinterDiscovery(
            discoveryWrapper = discoveryWrapper,
            discoveryTimeout = 0L
        )
    }

    test("discoverPrinter emits empty state initially") {
        every { discoveryWrapper.start(any(), any(), any()) } just Runs
        every { discoveryWrapper.stop() } just Runs

        val states = printerDiscovery.discoverPrinter(context).toList()

        states.size shouldBe 2
        states[0] shouldBe DiscoveryState.Idle
        states[1] shouldBe DiscoveryState.Discovered(emptyList())
        verify {
            discoveryWrapper.start(any(), any(), any())
            discoveryWrapper.stop()
        }
    }

    test("discoverPrinter discovers a printer and emits states") {
        val deviceInfo = mockk<DeviceInfo>(relaxed = true) {
            every { deviceType } returns Discovery.TYPE_PRINTER
            every { deviceName } returns "Epson TM-T88V"
            every { target } returns "TCP:192.168.1.100"
        }

        val callbackSlot = slot<(DeviceInfo) -> Unit>()
        every { discoveryWrapper.start(any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured(deviceInfo)
        }
        every { discoveryWrapper.stop() } just Runs

        val states = printerDiscovery.discoverPrinter(context).toList()

        states.size shouldBe 3
        states[0] shouldBe DiscoveryState.Idle
        states[1].let { state ->
            state.shouldBeInstanceOf<DiscoveryState.Discovering>()
            state.printers.size shouldBe 1
            state.printers[0].let { printer ->
                printer.manufacturer shouldBe EpsonManufacturer
                printer.connectionAddress shouldBe "192.168.1.100"
                printer.connectionType shouldBe ConnectionType.LAN
                printer.info.deviceModel shouldBe "Epson TM-T88V"
            }
        }
        states[2].let { state ->
            state.shouldBeInstanceOf<DiscoveryState.Discovered>()
            state.printers.size shouldBe 1
            state.printers[0].let { printer ->
                printer.manufacturer shouldBe EpsonManufacturer
                printer.connectionAddress shouldBe "192.168.1.100"
                printer.connectionType shouldBe ConnectionType.LAN
                printer.info.deviceModel shouldBe "Epson TM-T88V"
            }
        }
        verify {
            discoveryWrapper.start(any(), any(), any())
            discoveryWrapper.stop()
        }
    }

    test("discoverPrinter emits Error state on Epos2Exception") {
        every {
            discoveryWrapper.start(any(), any(), any())
        } throws Epos2Exception("Discovery failed", RuntimeException())

        every { discoveryWrapper.stop() } just Runs

        val states = printerDiscovery.discoverPrinter(context).toList()

        states.last().let { errorState ->
            errorState.shouldBeInstanceOf<DiscoveryState.Error>()
            errorState.message shouldBe "Discovery failed"
        }
        verify {
            discoveryWrapper.start(any(), any(), any())
            discoveryWrapper.stop()
        }
    }

    test("discoverPrinter handles invalid device info") {
        val invalidDeviceInfo = mockk<DeviceInfo>(relaxed = true) {
            every { deviceType } returns Discovery.TYPE_PRINTER
            every { deviceName } returns "" // Invalid: empty deviceName
            every { target } returns "TCP:192.168.1.100"
        }

        val callbackSlot = slot<(DeviceInfo) -> Unit>()
        every { discoveryWrapper.start(any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured(invalidDeviceInfo)
        }
        every { discoveryWrapper.stop() } returns Unit

        val states = printerDiscovery.discoverPrinter(context).toList()

        states[1] shouldBe DiscoveryState.Discovered(emptyList())
        verify {
            discoveryWrapper.start(any(), any(), any())
            discoveryWrapper.stop()
        }
    }

    test("discoverPrinter throws IllegalArgumentException for unsupported connection type") {
        val invalidDeviceInfo = mockk<DeviceInfo>(relaxed = true) {
            every { deviceType } returns Discovery.TYPE_PRINTER
            every { deviceName } returns "Epson TM-T88V"
            every { target } returns "INVALID:192.168.1.100" // Unsupported protocol
        }
        val callbackSlot = slot<(DeviceInfo) -> Unit>()
        every { discoveryWrapper.start(any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured(invalidDeviceInfo)
        }
        every { discoveryWrapper.stop() } returns Unit

        val exception = shouldThrow<IllegalArgumentException> {
            printerDiscovery.discoverPrinter(context).toList()
        }

        exception.message shouldBe "Unsupported connection type: INVALID"
    }
})

