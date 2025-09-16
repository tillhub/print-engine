package de.tillhub.printengine.epson

import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.ImageBitmap
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.RawPrinterData
import kotlinx.coroutines.flow.Flow

internal actual class EpsonPrinterController : PrinterController {
    actual override fun sendRawData(data: RawPrinterData) {
        TODO("Not yet implemented")
    }

    actual override fun observePrinterState(): Flow<PrinterState> {
        TODO("Not yet implemented")
    }

    actual override fun setFontSize(fontSize: PrintingFontType) {
        TODO("Not yet implemented")
    }

    actual override fun printText(text: String) {
        TODO("Not yet implemented")
    }

    actual override fun printBarcode(barcode: String) {
        TODO("Not yet implemented")
    }

    actual override fun printQr(qrData: String) {
        TODO("Not yet implemented")
    }

    actual override fun printImage(image: ImageBitmap) {
        TODO("Not yet implemented")
    }

    actual override fun feedPaper() {
        TODO("Not yet implemented")
    }

    actual override fun cutPaper() {
        TODO("Not yet implemented")
    }

    actual override fun setIntensity(intensity: PrintingIntensity) {
        TODO("Not yet implemented")
    }

    actual override fun start() {
        TODO("Not yet implemented")
    }

    actual override suspend fun getPrinterInfo(): PrinterInfo {
        TODO("Not yet implemented")
    }
}
