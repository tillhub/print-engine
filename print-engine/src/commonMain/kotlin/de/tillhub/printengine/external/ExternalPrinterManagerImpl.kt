package de.tillhub.printengine.external

import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.ExternalPrinter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

/**
 * Manages external printer discovery, combining results from multiple discovery methods.
 * Emits [DiscoveryState.Error] only if all discoveries fail without finding printers.
 */
internal class ExternalPrinterManagerImpl : ExternalPrinterManager {
    private val discoveredPrinters = mutableMapOf<String, ExternalPrinter>()

    override fun discoverExternalPrinters(vararg discoveries: PrinterDiscovery): Flow<DiscoveryState> = discoveries.map { it.observePrinters }.merge().map { state ->
        when (state) {
            DiscoveryState.Idle,
            is DiscoveryState.Error,
            -> state
            is DiscoveryState.Discovering -> {
                addUniquePrinters(state.printers)
                DiscoveryState.Discovering(discoveredPrinters.values.toList())
            }
            is DiscoveryState.Finished -> {
                addUniquePrinters(state.printers)
                DiscoveryState.Finished(discoveredPrinters.values.toList())
            }
        }
    }

    private fun addUniquePrinters(newPrinters: List<ExternalPrinter>) {
        newPrinters.forEach { printer ->
            discoveredPrinters[printer.connectionAddress] = printer
        }
    }
}
