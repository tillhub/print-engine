package de.tillhub.printengine.external

import android.content.Context
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.dispatcher.DispatcherProvider
import de.tillhub.printengine.dispatcher.DispatcherProviderImp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge

/**
 * Manages external printer discovery, combining results from multiple discovery methods.
 * Emits [DiscoveryState.Error] only if all discoveries fail without finding printers.
 */
internal class ExternalPrinterManagerImpl(
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider = DispatcherProviderImp()
) : ExternalPrinterManager {

    override fun discoverExternalPrinters(vararg discoveries: PrinterDiscovery): Flow<DiscoveryState> = flow {
        emit(DiscoveryState.Idle)

        if (discoveries.isEmpty()) {
            emit(DiscoveryState.Discovered(emptyList()))
            return@flow
        }

        val uniquePrinters = mutableListOf<ExternalPrinter>()
        var completedCount = 0
        var errorCount = 0

        discoveries
            .map { it.discoverPrinter(context) }
            .merge()
            .collect { state ->
                when (state) {
                    is DiscoveryState.Discovering -> {
                        addUniquePrinters(state.printers, uniquePrinters)
                        emit(DiscoveryState.Discovering(uniquePrinters.toList()))
                    }

                    is DiscoveryState.Discovered -> {
                        completedCount++
                        emitFinalStateIfAllDone(uniquePrinters, completedCount, errorCount, discoveries.size)
                    }

                    is DiscoveryState.Error -> {
                        errorCount++
                        emitFinalStateIfAllDone(uniquePrinters, completedCount, errorCount, discoveries.size)
                    }

                    is DiscoveryState.Idle -> Unit
                }
            }
    }.flowOn(dispatcherProvider.iO())

    private fun addUniquePrinters(newPrinters: List<ExternalPrinter>, uniquePrinters: MutableList<ExternalPrinter>) {
        newPrinters.forEach { printer ->
            if (uniquePrinters.none { it.connectionAddress == printer.connectionAddress }) {
                uniquePrinters.add(printer)
            }
        }
    }

    private suspend fun FlowCollector<DiscoveryState>.emitFinalStateIfAllDone(
        uniquePrinters: List<ExternalPrinter>,
        completedCount: Int,
        errorCount: Int,
        totalDiscoveries: Int
    ) {
        if (completedCount + errorCount == totalDiscoveries) {
            emit(
                uniquePrinters.takeIf { it.isNotEmpty() }
                    ?.let { DiscoveryState.Discovered(it) }
                    ?: DiscoveryState.Error("All discoveries failed")
            )
        }
    }
}