package de.tillhub.printengine.epson

import android.graphics.Bitmap
import com.epson.epos2.printer.Printer as EpsonPrinter

@Suppress("TooManyFunctions")
internal class EpsonPrinterWrapper(private val epsonPrinter: EpsonPrinter) {
    fun addCommand(bytes: ByteArray) {
        epsonPrinter.addCommand(bytes)
    }

    fun addTextFont(font: Int) {
        epsonPrinter.addTextFont(font)
    }

    fun addText(text: String) {
        epsonPrinter.addText(text)
    }

    fun addBarcode(
        barcode: String,
        barcodeType: Int,
        hriPosition: Int,
        font: Int,
        moduleWidth: Int,
        height: Int
    ) {
        epsonPrinter.addBarcode(barcode, barcodeType, hriPosition, font, moduleWidth, height)
    }

    fun addSymbol(
        data: String,
        symbolType: Int,
        level: Int,
        width: Int,
        height: Int,
        param: Int
    ) {
        epsonPrinter.addSymbol(data, symbolType, level, width, height, param)
    }

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

    fun addFeedLine(lines: Int) {
        epsonPrinter.addFeedLine(lines)
    }

    fun addCut(cutType: Int) {
        epsonPrinter.addCut(cutType)
    }

    fun connect(target: String, param: Int) {
        epsonPrinter.connect(target, param)
    }

    fun sendData(param: Int) {
        epsonPrinter.sendData(param)
    }

    fun clearCommandBuffer() {
        epsonPrinter.clearCommandBuffer()
    }

    fun disconnect() {
        epsonPrinter.disconnect()
    }

    val status: PrinterStatus
        get() = PrinterStatus(epsonPrinter.status.connection)
}

internal data class PrinterStatus(val connection: Int)