package de.tillhub.printengine.html

enum class FeedString(val value: String) {
    PAX("<br />"),
    VERIFONE("<br /><br /><br /><br /><br />")
}

enum class FontSize(val value: Int) {
    PAX(13),
    VERIFONE(20)
}

enum class QrCodeSize(val value: Int) {
    PAX(220),
    VERIFONE(420)
}

enum class BarcodeSize(val height: Int, val width: Int) {
    PAX(140, 435),
    VERIFONE(140, 435)
}