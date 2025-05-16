package de.tillhub.printengine.data

enum class ConnectionType {
    LAN_SECURED,
    LAN,
    BLUETOOTH,
    USB,
}

fun ConnectionType.fromConnectionType() = when (this) {
    ConnectionType.LAN_SECURED -> "TCPS"
    ConnectionType.LAN -> "TCP"
    ConnectionType.BLUETOOTH -> "BT"
    ConnectionType.USB -> "USB"
}

fun String.toConnectionType() = when (this) {
    "TCPS" -> ConnectionType.LAN_SECURED
    "TCP" -> ConnectionType.LAN
    "BT" -> ConnectionType.BLUETOOTH
    "USB" -> ConnectionType.USB
    else -> throw IllegalArgumentException("Unsupported connection type: $this")
}