package de.tillhub.printengine.data

enum class PrinterManufacturer(
    val value: String,
) {
    PAX("PAX"),
    SUNMI("SUNMI"),
    VERIFONE("Verifone"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {
        fun get(): PrinterManufacturer = entries.firstOrNull {
            it.value == getManufacturer()
        } ?: UNKNOWN
    }
}

expect fun getManufacturer(): String
