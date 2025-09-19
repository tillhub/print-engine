package de.tillhub.printengine.epson

import android.content.Context
import com.epson.epos2.Epos2Exception
import com.epson.epos2.discovery.DeviceInfo
import com.epson.epos2.discovery.Discovery
import com.epson.epos2.discovery.FilterOption
import de.tillhub.printengine.data.ConnectionType
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.external.PrinterDiscovery
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

actual class EpsonPrinterDiscovery(
    private val context: Context,
) : PrinterDiscovery {
    companion object {
        private const val CHARACTER_COUNT = 42
        private const val MANUFACTURER_EPSON = "EPSON"
    }

    /**
     * Epson discovery parameters.
     */
    private val discoveryFilters =
        FilterOption().apply {
            deviceType = Discovery.TYPE_PRINTER
            epsonFilter = Discovery.FILTER_NAME
            portType = Discovery.PORTTYPE_ALL
            deviceModel = Discovery.MODEL_ALL
        }

    actual override val observePrinters: Flow<DiscoveryState>
        get() =
            callbackFlow {
                trySend(DiscoveryState.Idle)

                val discoveredPrinters = mutableMapOf<String, ExternalPrinter>()

                try {
                    EpsonDiscoveryWrapper.start(context, discoveryFilters) { deviceInfo ->
                        if (deviceInfo.isValid()) {
                            with(createPrinter(deviceInfo)) {
                                // Use the connection address as a unique identifier
                                discoveredPrinters[connectionAddress] = this
                            }
                            trySend(DiscoveryState.Discovering(discoveredPrinters.values.toList()))
                        }
                    }
                } catch (e: Epos2Exception) {
                    trySend(DiscoveryState.Error(e.message))
                }

                awaitClose {
                    EpsonDiscoveryWrapper.stop()
                }
            }

    private fun createPrinter(deviceInfo: DeviceInfo): ExternalPrinter {
        val connectionDividerIdx = deviceInfo.target.indexOfFirst { it == ':' }
        val protocol = deviceInfo.target.substring(0, connectionDividerIdx)
        val address = deviceInfo.target.substring(connectionDividerIdx + 1)

        return ExternalPrinter(
            info =
            PrinterInfo(
                serialNumber = "n/a",
                deviceModel = deviceInfo.deviceName,
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

    private fun String.toConnectionType() = ConnectionType.entries.find { it.value == this }
        ?: throw IllegalArgumentException("Unsupported connection type: $this")

    private fun DeviceInfo.isValid() = deviceType == Discovery.TYPE_PRINTER && deviceName.isNotEmpty()
}
