package de.tillhub.printengine.data

enum class PrinterManufacturer(val value: String) {
    PAX("PAX"),
    SUNMI("SUNMI"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun get(): PrinterManufacturer =
            when (android.os.Build.MANUFACTURER) {
                SUNMI.value -> SUNMI
                PAX.value -> PAX
                else -> UNKNOWN
            }
    }
}