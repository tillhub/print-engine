package de.tillhub.printengine.html

@JvmInline
value class FeedString(val value: String)

@JvmInline
value class FontSize(val value: Int)

@JvmInline
value class QrCodeSize(val value: Int)

class BarcodeSize(val height: Int, val width: Int)
