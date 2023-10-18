package de.tillhub.printengine.data

enum class PrinterManufacturer(val value: String) {
    PAX("PAX"),
    SUNMI("SUNMI"),
    VERIFONE("Verifone"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun get(): PrinterManufacturer =
            values().firstOrNull {
                it.value == android.os.Build.MANUFACTURER
            } ?: UNKNOWN
    }
}
