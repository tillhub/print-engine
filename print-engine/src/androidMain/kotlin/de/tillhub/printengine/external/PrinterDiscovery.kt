package de.tillhub.printengine.external

import de.tillhub.printengine.data.DiscoveryState
import kotlinx.coroutines.flow.Flow

interface PrinterDiscovery {
    val observePrinters: Flow<DiscoveryState>
}