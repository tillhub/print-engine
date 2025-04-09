package de.tillhub.printengine.epson

import android.content.Context
import com.epson.epos2.Epos2Exception
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

object EpsonPrinterDiscovery : PrinterDiscovery {
    private const val DISCOVERY_TIMEOUT_MS = 10000L
    private const val CHARACTER_COUNT = 32

    private val discoveryFilters = FilterOption().apply {
        deviceType = Discovery.TYPE_PRINTER
        epsonFilter = Discovery.FILTER_NONE
        portType = Discovery.PORTTYPE_ALL
        deviceModel = Discovery.MODEL_ALL
    }

    private val discoveryScope = CoroutineScope(Dispatchers.IO)

    override suspend fun discoverPrinter(context: Context): Flow<DiscoveryState> = flow {
        emit(DiscoveryState.Idle)

        discoverAllPrinters(context, ::emit)
    }.flowOn(Dispatchers.IO)

    private suspend fun discoverAllPrinters(
        context: Context,
        emit: suspend (DiscoveryState) -> Unit
    ) {
        val discoveredPrinters = mutableListOf<ExternalPrinter>()

        try {
            Discovery.start(context, discoveryFilters) { deviceInfo ->
                val connectionDividerIdx = deviceInfo.target.indexOfFirst { it == ':' }
                val protocol = deviceInfo.target.substring(0, connectionDividerIdx)
                val address = deviceInfo.target.substring(connectionDividerIdx)

                discoveredPrinters.add(
                    ExternalPrinter(
                        info = PrinterInfo(
                            serialNumber = "n/a",
                            deviceModel = deviceInfo.deviceName,
                            printerVersion = "n/a",
                            printerPaperSpec = PrintingPaperSpec.External(CHARACTER_COUNT), // TODO
                            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
                            printerHead = "n/a",
                            printedDistance = 0,
                            serviceVersion = PrinterServiceVersion.Unknown
                        ),
                        manufacturer = EpsonManufacturer,
                        connectionAddress = address,
                        connectionType = protocol.toConnectionType(),
                    )
                )

                discoveryScope.launch {
                    emit(DiscoveryState.Discovering(discoveredPrinters))
                }
            }

            delay(DISCOVERY_TIMEOUT_MS)

            Discovery.stop()
            emit(DiscoveryState.Discovered(discoveredPrinters))
        } catch (e: Epos2Exception) {
            emit(DiscoveryState.Error(e.message))
            Discovery.stop()
        }
    }

    private fun String.toConnectionType() = when (this) {
        "TCP" -> ConnectionType.LAN
        "BT" -> ConnectionType.BLUETOOTH
        "USB" -> ConnectionType.USB
        else -> throw IllegalArgumentException("Unsupported connection type: $this")
    }
}