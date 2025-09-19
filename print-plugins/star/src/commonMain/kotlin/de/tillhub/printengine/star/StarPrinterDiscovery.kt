package de.tillhub.printengine.star

import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.external.PrinterDiscovery
import kotlinx.coroutines.flow.Flow

expect class StarPrinterDiscovery : PrinterDiscovery {
    override val observePrinters: Flow<DiscoveryState>
}
