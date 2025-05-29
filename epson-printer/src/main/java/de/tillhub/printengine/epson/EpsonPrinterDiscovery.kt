package de.tillhub.printengine.epson

import android.content.Context
import com.epson.epos2.Epos2Exception
import com.epson.epos2.discovery.DeviceInfo
import com.epson.epos2.discovery.Discovery
import com.epson.epos2.discovery.FilterOption
import de.tillhub.printengine.data.ConnectionType
import de.tillhub.printengine.data.ConnectionType.entries
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.external.PrinterDiscovery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import org.jetbrains.annotations.VisibleForTesting

object EpsonPrinterDiscovery : PrinterDiscovery {

    private const val DISCOVERY_TIMEOUT_MS = 10000L
    private const val CHARACTER_COUNT = 42
    private const val MANUFACTURER_EPSON = "EPSON"

    private val discoveryFilters = FilterOption().apply {
        deviceType = Discovery.TYPE_PRINTER
        epsonFilter = Discovery.FILTER_NAME
        portType = Discovery.PORTTYPE_ALL
        deviceModel = Discovery.MODEL_ALL
    }
    private var discoveryTimeoutMs: Long = DISCOVERY_TIMEOUT_MS

    override suspend fun discoverPrinter(context: Context): Flow<DiscoveryState> = channelFlow {
        trySend(DiscoveryState.Idle)

        discoverAllPrinters(context, ::trySend)
    }.flowOn(Dispatchers.IO)

    private suspend fun discoverAllPrinters(
        context: Context,
        trySend: (DiscoveryState) -> Unit,
    ) {
        var discoveredPrinters: List<ExternalPrinter> = emptyList()

        try {
            EpsonDiscoveryWrapper.start(context, discoveryFilters) { deviceInfo ->
                if (deviceInfo.isValid()) {
                    val connectionDividerIdx = deviceInfo.target.indexOfFirst { it == ':' }
                    val protocol = deviceInfo.target.substring(0, connectionDividerIdx)
                    val address = deviceInfo.target.substring(connectionDividerIdx + 1)

                    val externalPrinter = ExternalPrinter(
                        info = PrinterInfo(
                            serialNumber = "n/a",
                            deviceModel = deviceInfo.deviceName,
                            printerVersion = "n/a",
                            printerPaperSpec = PrintingPaperSpec.External(CHARACTER_COUNT),
                            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
                            printerHead = "n/a",
                            printedDistance = 0,
                            serviceVersion = PrinterServiceVersion.Unknown
                        ),
                        manufacturer = MANUFACTURER_EPSON,
                        connectionAddress = address,
                        connectionType = protocol.toConnectionType(),
                    )

                    discoveredPrinters = addPrinter(externalPrinter, discoveredPrinters)
                    trySend(DiscoveryState.Discovering(discoveredPrinters.toList()))
                }
            }

            delay(discoveryTimeoutMs)

            EpsonDiscoveryWrapper.stop()
            trySend(DiscoveryState.Discovered(discoveredPrinters.toList()))
        } catch (e: Epos2Exception) {
            trySend(DiscoveryState.Error(e.message))
            EpsonDiscoveryWrapper.stop()
        }
    }

    private fun addPrinter(
        newPrinter: ExternalPrinter,
        existingPrinters: List<ExternalPrinter>
    ): List<ExternalPrinter> {
        if (existingPrinters.any { it.connectionAddress == newPrinter.connectionAddress }) {
            return existingPrinters
        }
        return existingPrinters + newPrinter
    }

    private fun String.toConnectionType() = ConnectionType.entries.find { it.value == this }
        ?: throw IllegalArgumentException("Unsupported connection type: $this")

    @VisibleForTesting
    fun setDiscoveryTimeout(timeoutMs: Long) {
        discoveryTimeoutMs = timeoutMs
    }

    private fun DeviceInfo.isValid() =
        deviceType == Discovery.TYPE_PRINTER && deviceName.isNotEmpty()
}