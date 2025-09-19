package de.tillhub.printengine.star

import android.content.Context
import com.starmicronics.stario10.InterfaceType
import com.starmicronics.stario10.StarDeviceDiscoveryManager
import com.starmicronics.stario10.StarDeviceDiscoveryManagerFactory
import com.starmicronics.stario10.StarIO10Exception
import com.starmicronics.stario10.StarPrinter
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

actual class StarPrinterDiscovery(
    private val context: Context,
) : PrinterDiscovery {
    companion object {
        private const val CHARACTER_COUNT = 52
        private const val MANUFACTURER_STAR = "STAR"
    }

    /**
     * List of interface types to discover.
     * You can modify this list to include or exclude specific interfaces.
     */
    private val interfaceTypes =
        listOf(InterfaceType.Lan, InterfaceType.Bluetooth, InterfaceType.Usb)

    actual override val observePrinters: Flow<DiscoveryState>
        get() =
            callbackFlow {
                trySend(DiscoveryState.Idle)

                val discoveredPrinters = mutableMapOf<String, ExternalPrinter>()
                val discoveryManager =
                    try {
                        StarDeviceDiscoveryManagerFactory.create(interfaceTypes, context)
                    } catch (e: StarIO10Exception) {
                        trySend(DiscoveryState.Error(e.message))
                        return@callbackFlow
                    }

                discoveryManager.callback =
                    object : StarDeviceDiscoveryManager.Callback {
                        override fun onPrinterFound(printer: StarPrinter) {
                            with(createPrinter(printer)) {
                                discoveredPrinters[connectionAddress] = this
                            }
                            trySend(DiscoveryState.Discovering(discoveredPrinters.values.toList()))
                        }

                        override fun onDiscoveryFinished() {
                            trySend(DiscoveryState.Finished(discoveredPrinters.values.toList()))
                        }
                    }

                try {
                    discoveryManager.startDiscovery()
                } catch (e: StarIO10Exception) {
                    trySend(DiscoveryState.Error(e.message))
                }

                awaitClose {
                    discoveryManager.callback = null
                    discoveryManager.stopDiscovery()
                }
            }

    private fun createPrinter(printer: StarPrinter) = ExternalPrinter(
        info =
        PrinterInfo(
            serialNumber = "n/a",
            deviceModel = printer.information?.model?.name ?: "Unknown",
            printerVersion = "n/a",
            printerPaperSpec = PrintingPaperSpec.External(characterCount = CHARACTER_COUNT),
            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
            printerHead = "n/a",
            printedDistance = 0,
            serviceVersion = PrinterServiceVersion.Unknown,
        ),
        manufacturer = MANUFACTURER_STAR,
        connectionAddress = printer.connectionSettings.identifier,
        connectionType = printer.connectionSettings.interfaceType.toConnectionType(),
    )

    private fun InterfaceType.toConnectionType(): ConnectionType = when (this) {
        InterfaceType.Lan -> ConnectionType.LAN
        InterfaceType.Bluetooth -> ConnectionType.BLUETOOTH
        InterfaceType.Usb -> ConnectionType.USB
        else -> throw IllegalArgumentException("Unsupported interface type: $this")
    }
}
