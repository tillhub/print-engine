package de.tillhub.printengine.barcode

object BarcodeEncoderProvider {
    fun get(): BarcodeEncoder = BarcodeEncoderImpl()
}
