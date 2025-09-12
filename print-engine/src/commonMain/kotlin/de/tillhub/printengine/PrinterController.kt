package de.tillhub.printengine

import de.tillhub.printengine.data.ImageBitmap
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.RawPrinterData
import kotlinx.coroutines.flow.Flow

interface PrinterController {
    fun sendRawData(data: RawPrinterData)

    fun observePrinterState(): Flow<PrinterState>
    fun setFontSize(fontSize: PrintingFontType)
    fun printText(text: String)
    fun printBarcode(barcode: String)
    fun printQr(qrData: String)
    fun printImage(image: ImageBitmap)
    fun feedPaper()
    fun cutPaper()
    fun setIntensity(intensity: PrintingIntensity)
    fun start()
    suspend fun getPrinterInfo(): PrinterInfo
}