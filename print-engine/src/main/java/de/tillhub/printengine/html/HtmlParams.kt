package de.tillhub.printengine.html

internal enum class FeedString(val value: String) {
    PAX("<br />"),
    VERIFONE("<br /><br /><br /><br /><br />")
}

internal enum class FontSize(val value: Int) {
    PAX(13),
    VERIFONE(20)
}

internal enum class QrCodeSize(val value: Int) {
    PAX(220),
    VERIFONE(420)
}

internal enum class BarcodeSize(val height: Int, val width: Int) {
    PAX(190, 420),
    VERIFONE(140, 420)
}