package de.tillhub.printengine

import android.graphics.Bitmap
import de.tillhub.printengine.data.*

interface PrinterController {
    fun sendRawData(data: RawPrinterData)
    fun getPrinterState(): PrinterState
    fun setFontSize(fontSize: PrintingFontType)
    fun printText(text: String)
    fun printBarcode(barcode: String)
    fun printQr(qrData: String)
    fun printImage(image: Bitmap)
    fun feedPaper()
    fun cutPaper()
    fun setIntensity(intensity: PrintingIntensity)
    fun start()
    suspend fun getPrinterInfo(): PrinterInfo
}