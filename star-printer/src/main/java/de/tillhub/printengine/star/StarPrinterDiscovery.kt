package de.tillhub.printengine.star

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.starmicronics.stario10.InterfaceType
import com.starmicronics.stario10.StarDeviceDiscoveryManager
import com.starmicronics.stario10.StarDeviceDiscoveryManagerFactory
import com.starmicronics.stario10.StarPrinter
import de.tillhub.printengine.data.ConnectionType
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

object StarPrinterDiscovery : PrinterDiscovery {

    private const val DISCOVERY_TIMEOUT_MS = 10000L
    private const val CHARACTER_COUNT = 32
    private const val MANUFACTURER_STAR = "STAR"

    private var discoveryTimeoutMs: Long = DISCOVERY_TIMEOUT_MS

    override suspend fun discoverPrinter(context: Context): Flow<DiscoveryState> = channelFlow {
        trySend(DiscoveryState.Idle)
        discoverAllPrinters(context, ::trySend)
    }.flowOn(Dispatchers.IO)

    private suspend fun discoverAllPrinters(
        context: Context,
        trySend: (DiscoveryState) -> Unit,
    ) {
        trySend(DiscoveryState.Discovered(emptyList()))
    }

    private fun InterfaceType.toConnectionType(): ConnectionType = when (this) {
        InterfaceType.Lan -> ConnectionType.LAN
        InterfaceType.Bluetooth -> ConnectionType.BLUETOOTH
        InterfaceType.Usb -> ConnectionType.USB
        else -> throw IllegalArgumentException("Unsupported interface type: $this")
    }

    @VisibleForTesting
    fun setDiscoveryTimeout(timeoutMs: Long) {
        discoveryTimeoutMs = timeoutMs
    }
}

