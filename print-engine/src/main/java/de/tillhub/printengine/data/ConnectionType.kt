package de.tillhub.printengine.data

enum class ConnectionType(val value: String) {
    LAN_SECURED("TCPS"),
    LAN("TCP"),
    BLUETOOTH("BT"),
    USB("USB")
}