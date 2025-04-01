package de.tillhub.printengine.external

import android.content.Context
import de.tillhub.printengine.data.DiscoveryState
import kotlinx.coroutines.flow.Flow

interface PrinterDiscovery {
    suspend fun discoverPrinter(
        context: Context,
    ): Flow<DiscoveryState>
}