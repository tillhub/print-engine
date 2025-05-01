package de.tillhub.printengine.epson

import android.graphics.Bitmap
import com.epson.epos2.printer.Printer as EpsonPrinter

interface PrinterWrapper {
    fun addCommand(bytes: ByteArray)
    fun addTextFont(font: Int)
    fun addText(text: String)
    fun addBarcode(
        barcode: String,
        barcodeType: Int,
        hriPosition: Int,
        font: Int,
        moduleWidth: Int,
        height: Int
    )

    fun addSymbol(
        data: String,
        symbolType: Int,
        level: Int,
        width: Int,
        height: Int,
        param: Int
    )

    fun addImage(
        bitmap: Bitmap,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: Int,
        mode: Int,
        halftone: Int,
        brightness: Double,
        compress: Int
    )

    fun addFeedLine(lines: Int)
    fun addCut(cutType: Int)
    fun connect(target: String, param: Int)
    fun sendData(param: Int)
    fun clearCommandBuffer()
    fun disconnect()
    val status: PrinterStatus
}

data class PrinterStatus(val connection: Int)

class EpsonPrinterWrapper(private val epsonPrinter: EpsonPrinter) : PrinterWrapper {
    override fun addCommand(bytes: ByteArray) {
        epsonPrinter.addCommand(bytes)
    }

    override fun addTextFont(font: Int) {
        epsonPrinter.addTextFont(font)
    }

    override fun addText(text: String) {
        epsonPrinter.addText(text)
    }

    override fun addBarcode(
        barcode: String,
        barcodeType: Int,
        hriPosition: Int,
        font: Int,
        moduleWidth: Int,
        height: Int
    ) {
        epsonPrinter.addBarcode(barcode, barcodeType, hriPosition, font, moduleWidth, height)
    }

    override fun addSymbol(
        data: String,
        symbolType: Int,
        level: Int,
        width: Int,
        height: Int,
        param: Int
    ) {
        epsonPrinter.addSymbol(data, symbolType, level, width, height, param)
    }

    override fun addImage(
        bitmap: Bitmap,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: Int,
        mode: Int,
        halftone: Int,
        brightness: Double,
        compress: Int
    ) {
        epsonPrinter.addImage(
            bitmap,
            x,
            y,
            width,
            height,
            color,
            mode,
            halftone,
            brightness,
            compress
        )
    }

    override fun addFeedLine(lines: Int) {
        epsonPrinter.addFeedLine(lines)
    }

    override fun addCut(cutType: Int) {
        epsonPrinter.addCut(cutType)
    }

    override fun connect(target: String, param: Int) {
        epsonPrinter.connect(target, param)
    }

    override fun sendData(param: Int) {
        epsonPrinter.sendData(param)
    }

    override fun clearCommandBuffer() {
        epsonPrinter.clearCommandBuffer()
    }

    override fun disconnect() {
        epsonPrinter.disconnect()
    }

    override val status: PrinterStatus
        get() = PrinterStatus(epsonPrinter.status.connection)
}