package de.tillhub.printengine.data

sealed class DiscoveryState {
    data object Idle : DiscoveryState()
    class Error(val message: String?) : DiscoveryState()
    class Discovering(val printers: List<ExternalPrinter>) : DiscoveryState()
    class Discovered(val printers: List<ExternalPrinter>) : DiscoveryState()
}