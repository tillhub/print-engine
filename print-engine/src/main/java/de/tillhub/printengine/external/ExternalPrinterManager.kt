package de.tillhub.printengine.external

import de.tillhub.printengine.data.DiscoveryState
import kotlinx.coroutines.flow.Flow

internal interface ExternalPrinterManager {
    fun discoverExternalPrinters(vararg discoveries: PrinterDiscovery): Flow<DiscoveryState>
}