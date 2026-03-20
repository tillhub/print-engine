package de.tillhub.printengine.epson

import com.epson.epos2.Epos2DeviceInfo
import com.epson.epos2.Epos2Discovery
import com.epson.epos2.Epos2DiscoveryDelegateProtocol
import com.epson.epos2.Epos2FilterOption
import de.tillhub.printengine.data.ConnectionType
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.external.PrinterDiscovery
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
actual class EpsonPrinterDiscovery : PrinterDiscovery {

    private companion object {
        const val CHARACTER_COUNT = 42
        const val MANUFACTURER_EPSON = "EPSON"

        // ePOS2.h enum values
        const val EPOS2_SUCCESS = 0
        const val EPOS2_PORTTYPE_ALL = 0
        const val EPOS2_TYPE_PRINTER = 1
        const val EPOS2_MODEL_ALL = 0
    }

    actual override val observePrinters: Flow<DiscoveryState>
        get() = callbackFlow {
            trySend(DiscoveryState.Idle)

            val discoveredPrinters = mutableMapOf<String, ExternalPrinter>()

            val filterOption = Epos2FilterOption().apply {
                portType = EPOS2_PORTTYPE_ALL
                deviceType = EPOS2_TYPE_PRINTER
                deviceModel = EPOS2_MODEL_ALL
            }

            // Epson SDK dispatches onDiscovery callbacks sequentially on its internal thread
            val discoveryDelegate = object : NSObject(), Epos2DiscoveryDelegateProtocol {
                override fun onDiscovery(deviceInfo: Epos2DeviceInfo?) {
                    val info = deviceInfo ?: return
                    if (!info.isValid()) return
                    val printer = createPrinter(info)
                    discoveredPrinters[printer.connectionAddress] = printer
                    trySend(DiscoveryState.Discovering(discoveredPrinters.values.toList()))
                }
            }

            val result = withContext(Dispatchers.Main) {
                Epos2Discovery.start(filterOption, discoveryDelegate)
            }
            if (result != EPOS2_SUCCESS) {
                trySend(DiscoveryState.Error("Discovery start failed with code: $result"))
            }

            awaitClose {
                Epos2Discovery.stop()
                // Reference delegate to prevent Kotlin/Native GC from collecting it
                // while the Epson SDK still holds a weak reference to it
                discoveryDelegate.hashCode()
            }
        }

    private fun createPrinter(deviceInfo: Epos2DeviceInfo): ExternalPrinter {
        val target = deviceInfo.target ?: ""
        val dividerIdx = target.indexOf(':')
        val protocol = if (dividerIdx >= 0) target.substring(0, dividerIdx) else ""
        val address = if (dividerIdx >= 0) target.substring(dividerIdx + 1) else target

        return ExternalPrinter(
            info = PrinterInfo(
                serialNumber = "n/a",
                deviceModel = deviceInfo.deviceName ?: "",
                printerVersion = "n/a",
                printerPaperSpec = PrintingPaperSpec.External(CHARACTER_COUNT),
                printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
                printerHead = "n/a",
                printedDistance = 0,
                serviceVersion = PrinterServiceVersion.Unknown,
            ),
            manufacturer = MANUFACTURER_EPSON,
            connectionAddress = address,
            connectionType = protocol.toConnectionType(),
        )
    }

    private fun String.toConnectionType(): ConnectionType = ConnectionType.entries.find { it.value == this }
        ?: ConnectionType.LAN

    private fun Epos2DeviceInfo.isValid(): Boolean = deviceType == EPOS2_TYPE_PRINTER && !deviceName.isNullOrEmpty()
}
