package de.tillhub.printengine.html

import kotlin.jvm.JvmInline

@JvmInline
value class FeedString(
    val value: String,
)

@JvmInline
value class FontSize(
    val value: Int,
)

@JvmInline
value class QrCodeSize(
    val value: Int,
)

class BarcodeSize(
    val height: Int,
    val width: Int,
)
