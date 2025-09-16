package de.tillhub.printengine.epson

import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.external.PrinterDiscovery
import kotlinx.coroutines.flow.Flow

actual class EpsonPrinterDiscovery : PrinterDiscovery {
    actual override val observePrinters: Flow<DiscoveryState>
        get() = TODO("Not yet implemented")
}
