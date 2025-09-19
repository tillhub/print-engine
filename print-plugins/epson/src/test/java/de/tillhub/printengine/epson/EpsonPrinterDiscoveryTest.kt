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
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@ExperimentalCoroutinesApi
class EpsonPrinterDiscoveryTest : FunSpec({

    lateinit var context: Context
    lateinit var discovery: EpsonPrinterDiscovery
    lateinit var mockDeviceInfo: DeviceInfo
    val testScope = TestScope(UnconfinedTestDispatcher())

    beforeEach {
        mockkObject(EpsonDiscoveryWrapper)
        every { EpsonDiscoveryWrapper.start(any(), any(), any()) } just Runs
        every { EpsonDiscoveryWrapper.stop() } just Runs
        context = mockk()
        discovery = EpsonPrinterDiscovery(context)
        mockDeviceInfo = mockk {
            every { deviceType } returns Discovery.TYPE_PRINTER
            every { deviceName } returns "TM-T88V"
            every { target } returns "TCP:192.168.1.100"
        }
    }

    afterEach {
        unmockkObject(EpsonDiscoveryWrapper)
    }

    test("observePrinters emits Idle state initially") {
        testScope.runTest {
            val states = discovery.observePrinters.take(1).toList()
            states.size shouldBe 1
            states[0] shouldBe DiscoveryState.Idle
            advanceUntilIdle()
            verify {
                EpsonDiscoveryWrapper.start(any(), any(), any())
            }
        }
    }

    test("observePrinters discovers a printer and emits states") {
            val callbackSlot = slot<(DeviceInfo) -> Unit>()
            every { EpsonDiscoveryWrapper.start(any(), any(), capture(callbackSlot)) } answers {
                callbackSlot.captured(mockDeviceInfo)
                Runs
            }

            val states = discovery.observePrinters.take(2).toList()

            states.size shouldBe 2
            states[0] shouldBe DiscoveryState.Idle
            states[1].let { state ->
                state.shouldBeInstanceOf<DiscoveryState.Discovering>()
                state.printers.size shouldBe 1
                state.printers[0].let { printer ->
                    printer.manufacturer shouldBe "EPSON"
                    printer.connectionAddress shouldBe "192.168.1.100"
                    printer.connectionType shouldBe ConnectionType.LAN
                    printer.info.deviceModel shouldBe "TM-T88V"
                }
            }
            verify {
                EpsonDiscoveryWrapper.start(any(), any(), any())
                EpsonDiscoveryWrapper.stop()
            }
        }
    test("discoverPrinter emits Error state on Epos2Exception") {
        every {
            EpsonDiscoveryWrapper.start(any(), any(), any())
        } throws Epos2Exception("Discovery failed", RuntimeException())

        every { EpsonDiscoveryWrapper.stop() } just Runs

        val states = discovery.observePrinters.take(2).toList()

        states.last().let { errorState ->
            errorState.shouldBeInstanceOf<DiscoveryState.Error>()
            errorState.message shouldBe "Discovery failed"
        }
        verify {
            EpsonDiscoveryWrapper.start(any(), any(), any())
            EpsonDiscoveryWrapper.stop()
        }
    }

    test("discoverPrinter handles invalid device info") {
        val invalidDeviceInfo = mockk<DeviceInfo>(relaxed = true) {
            every { deviceType } returns Discovery.TYPE_PRINTER
            every { deviceName } returns "" // Invalid: empty deviceName
            every { target } returns "TCP:192.168.1.100"
        }

        val callbackSlot = slot<(DeviceInfo) -> Unit>()
        every { EpsonDiscoveryWrapper.start(any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured(invalidDeviceInfo)
        }

        val states = discovery.observePrinters.take(1).toList()

        states.last() shouldBe DiscoveryState.Idle
        verify {
            EpsonDiscoveryWrapper.start(any(), any(), any())
        }
    }

    test("discoverPrinter throws IllegalArgumentException for unsupported connection type") {
        val invalidDeviceInfo = mockk<DeviceInfo>(relaxed = true) {
            every { deviceType } returns Discovery.TYPE_PRINTER
            every { deviceName } returns "Epson TM-T88V"
            every { target } returns "INVALID:192.168.1.100" // Unsupported protocol
        }
        val callbackSlot = slot<(DeviceInfo) -> Unit>()
        every { EpsonDiscoveryWrapper.start(any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured(invalidDeviceInfo)
        }
        every { EpsonDiscoveryWrapper.stop() } returns Unit

        val exception = shouldThrow<IllegalArgumentException> {
            discovery.observePrinters.take(1).toList()
        }

        exception.message shouldBe "Unsupported connection type: INVALID"
    }
})
