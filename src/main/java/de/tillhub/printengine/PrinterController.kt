package de.tillhub.printengine

import android.graphics.Bitmap
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.RawPrinterData

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
    fun start()
    suspend fun getPrinterInfo(): PrinterInfo
}