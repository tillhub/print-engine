package de.tillhub.printengine.external

import android.content.Context
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.dispatcher.DispatcherProvider
import de.tillhub.printengine.dispatcher.DispatcherProviderImp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge

internal class ExternalPrinterManagerImpl(
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider = DispatcherProviderImp()
) : ExternalPrinterManager {
    override fun discoverExternalPrinters(vararg discoveries: PrinterDiscovery): Flow<DiscoveryState> =
        flow {
            emit(DiscoveryState.Idle)

            val allPrinters = mutableListOf<ExternalPrinter>()
            var completedDiscoveries = 0

            discoveries.map { discovery ->
                discovery.discoverPrinter(context)
            }.merge().collect { state ->
                when (state) {
                    is DiscoveryState.Discovering -> {
                        state.printers.forEach { printer ->
                            if (allPrinters.none { it.connectionAddress == printer.connectionAddress }) {
                                allPrinters.add(printer)
                            }
                        }
                        emit(DiscoveryState.Discovering(allPrinters.toList()))
                    }

                    is DiscoveryState.Discovered -> {
                        completedDiscoveries++
                        if (completedDiscoveries == discoveries.size) {
                            emit(DiscoveryState.Discovered(allPrinters.toList()))
                        }
                    }

                    is DiscoveryState.Error -> Unit
                    is DiscoveryState.Idle -> Unit
                }
            }
        }.flowOn(dispatcherProvider.iO())
}