package de.tillhub.printengine.data

import de.tillhub.printengine.helpers.HashHelper

sealed class DiscoveryState {
    data object Idle : DiscoveryState()

    class Error(
        val message: String?,
    ) : DiscoveryState() {
        override fun toString() = "DiscoveryState.Error(" +
            "message=$message" +
            ")"

        override fun equals(other: Any?) = other is Error &&
            message == other.message

        override fun hashCode() = message.hashCode()
    }

    class Discovering(
        val printers: List<ExternalPrinter>,
    ) : DiscoveryState() {
        override fun toString() = "DiscoveryState.Discovering(" +
            "printers=$printers" +
            ")"

        override fun equals(other: Any?) = other is Discovering &&
            printers == other.printers

        override fun hashCode() = HashHelper.hash(printers)
    }

    /**
     * Represents the final state of discovery, containing a list of discovered printers.
     * This state is emitted when the discovery process is complete and it's optional (supported by some printers).
     * This state might not be the final state for other printers that continue to discover printers.
     */
    class Finished(
        val printers: List<ExternalPrinter>,
    ) : DiscoveryState() {
        override fun toString() = "DiscoveryState.Discovered(" +
            "printers=$printers" +
            ")"

        override fun equals(other: Any?) = other is Finished &&
            printers == other.printers

        override fun hashCode() = HashHelper.hash(printers)
    }
}
