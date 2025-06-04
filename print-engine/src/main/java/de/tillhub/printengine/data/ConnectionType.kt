package de.tillhub.printengine.data

enum class ConnectionType(val value: String) {
    LAN_SECURED("TCPS"),
    LAN("TCP"),
    BLUETOOTH("BT"),
    USB("USB");

    companion object {
        fun toConnectionType(value: String) = ConnectionType.entries.find { it.value == value }
            ?: throw IllegalArgumentException("ConnectionType not found for value: $this")
    }
}