package de.tillhub.printengine.data

import java.util.Objects

sealed class DiscoveryState {
    data object Idle : DiscoveryState()
    class Error(val message: String?) : DiscoveryState() {
        override fun toString() = "DiscoveryState.Error(" +
                "message=$message" +
                ")"

        override fun equals(other: Any?) = other is Error &&
                message == other.message

        override fun hashCode() = message.hashCode()
    }

    class Discovering(val printers: List<ExternalPrinter>) : DiscoveryState() {
        override fun toString() = "DiscoveryState.Discovering(" +
                "printers=$printers" +
                ")"

        override fun equals(other: Any?) = other is Discovering &&
                printers == other.printers

        override fun hashCode() = Objects.hash(printers)
    }

    class Discovered(val printers: List<ExternalPrinter>) : DiscoveryState() {
        override fun toString() = "DiscoveryState.Discovered(" +
                "printers=$printers" +
                ")"

        override fun equals(other: Any?) = other is Discovered &&
                printers == other.printers

        override fun hashCode() = Objects.hash(printers)
    }
}