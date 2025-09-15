package de.tillhub.printengine.epson

import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.external.PrinterDiscovery
import kotlinx.coroutines.flow.Flow

expect class EpsonPrinterDiscovery : PrinterDiscovery {
    override val observePrinters: Flow<DiscoveryState>
}