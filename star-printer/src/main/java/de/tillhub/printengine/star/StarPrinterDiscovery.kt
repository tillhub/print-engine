package de.tillhub.printengine.star

import android.content.Context
import com.starmicronics.stario10.InterfaceType
import com.starmicronics.stario10.StarDeviceDiscoveryManager
import com.starmicronics.stario10.StarDeviceDiscoveryManagerFactory
import com.starmicronics.stario10.StarPrinter
import de.tillhub.printengine.data.ConnectionType
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.external.PrinterDiscovery
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingPaperSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

object StarPrinterDiscovery : PrinterDiscovery {
    private const val DISCOVERY_TIMEOUT_MS = 10000

    override suspend fun discoverPrinter(context: Context): Flow<DiscoveryState> =
        withContext(Dispatchers.IO) {
            flow {
                emit(DiscoveryState.Idle)
                val interfaceTypes = listOf(InterfaceType.Lan, InterfaceType.Bluetooth, InterfaceType.Usb)
                val discoveryManager = StarDeviceDiscoveryManagerFactory.create(interfaceTypes, context)
                    .apply { discoveryTime = DISCOVERY_TIMEOUT_MS }

                emitAll(discoverPrintersFlow(discoveryManager))
            }
        }

    private fun discoverPrintersFlow(manager: StarDeviceDiscoveryManager): Flow<DiscoveryState> =
        callbackFlow {
            manager.callback = object : StarDeviceDiscoveryManager.Callback {
                override fun onPrinterFound(printer: StarPrinter) {
                    val externalPrinter = ExternalPrinter(
                        info = PrinterInfo(
                            serialNumber = "n/a",
                            deviceModel = printer.information?.model?.name ?: "Unknown",
                            printerVersion = "n/a",
                            printerPaperSpec = PrintingPaperSpec.External(characterCount = 32),
                            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
                            printerHead = "n/a",
                            printedDistance = 0,
                            serviceVersion = PrinterServiceVersion.Unknown
                        ),
                        manufacturer = StarManufacturer,
                        connectionAddress = printer.connectionSettings.identifier,
                        connectionType = printer.connectionSettings.interfaceType.toConnectionType()
                    )
                    trySend(DiscoveryState.Discovering(listOf(externalPrinter)))
                }

                override fun onDiscoveryFinished() {
                    trySend(DiscoveryState.Discovered(emptyList()))
                    close()
                }
            }

            runCatching {
                manager.startDiscovery()
            }.onFailure {
                trySend(DiscoveryState.Error(it.message))
                close()
            }
            awaitClose {
                manager.stopDiscovery()
            }
        }
}

private fun InterfaceType.toConnectionType(): ConnectionType = when (this) {
    InterfaceType.Lan -> ConnectionType.LAN
    InterfaceType.Bluetooth -> ConnectionType.BLUETOOTH
    InterfaceType.Usb -> ConnectionType.USB
    else -> throw IllegalArgumentException("Unsupported interface type: $this")
}
