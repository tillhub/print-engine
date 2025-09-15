package de.tillhub.printengine.star

import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.ImageBitmap
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.RawPrinterData
import kotlinx.coroutines.flow.Flow

internal expect class StarPrinterController : PrinterController{
    override fun sendRawData(data: RawPrinterData)
    override fun observePrinterState(): Flow<PrinterState>
    override fun setFontSize(fontSize: PrintingFontType)
    override fun printText(text: String)
    override fun printBarcode(barcode: String)
    override fun printQr(qrData: String)
    override fun printImage(image: ImageBitmap)
    override fun feedPaper()
    override fun cutPaper()
    override fun setIntensity(intensity: PrintingIntensity)
    override fun start()
    override suspend fun getPrinterInfo(): PrinterInfo
}