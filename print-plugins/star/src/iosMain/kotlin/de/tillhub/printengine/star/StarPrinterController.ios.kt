package de.tillhub.printengine.star

import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.ImageBitmap
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.RawPrinterData
import kotlinx.coroutines.flow.Flow

internal actual class StarPrinterController : PrinterController {
    actual override fun sendRawData(data: RawPrinterData) {
    }

    actual override fun observePrinterState(): Flow<PrinterState> {
        TODO("Not yet implemented")
    }

    actual override fun setFontSize(fontSize: PrintingFontType) {
    }

    actual override fun printText(text: String) {
    }

    actual override fun printBarcode(barcode: String) {
    }

    actual override fun printQr(qrData: String) {
    }

    actual override fun printImage(image: ImageBitmap) {
    }

    actual override fun feedPaper() {
    }

    actual override fun cutPaper() {
    }

    actual override fun setIntensity(intensity: PrintingIntensity) {
    }

    actual override fun start() {
    }

    actual override suspend fun getPrinterInfo(): PrinterInfo {
        TODO("Not yet implemented")
    }
}
