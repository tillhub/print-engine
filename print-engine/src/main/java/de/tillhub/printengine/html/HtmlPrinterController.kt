package de.tillhub.printengine.html

import android.graphics.Bitmap
import de.tillhub.printengine.html.HtmlUtils.monospaceText
import de.tillhub.printengine.html.HtmlUtils.singleLineCenteredText
import de.tillhub.printengine.html.HtmlUtils.transformToHtml
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.barcode.BarcodeType
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.RawPrinterData
import de.tillhub.printengine.html.HtmlUtils.generateImageHtml
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal abstract class HtmlPrinterController(
    private val printerState: MutableStateFlow<PrinterState>,
    private val barcodeEncoder: BarcodeEncoder,
    protected val barcodeSize: BarcodeSize,
    protected val qrCodeSize: QrCodeSize,
    protected val fontSize: FontSize,
    protected val includeStyleTag: Boolean,
    protected val feedString: FeedString,
) : PrinterController {

    private val batchSB = StringBuilder()

    protected abstract fun printContent(content: String, cutAfterPrint: Boolean = false)

    override fun observePrinterState(): StateFlow<PrinterState> = printerState

    override fun sendRawData(data: RawPrinterData) {
        printText(data.bytes.toString(Charsets.UTF_8))
    }

    override fun printText(text: String) {
        batchSB.appendLine(monospaceText(text, fontSize.value))
    }

    override fun printImage(image: Bitmap) {
        batchSB.append(generateImageHtml(image))
    }

    override fun printBarcode(barcode: String) {
        barcodeEncoder.encodeAsBitmap(barcode, BarcodeType.CODE_128, barcodeSize.width, barcodeSize.height)?.let { image ->
            printImage(image)
            printText(singleLineCenteredText(barcode))
        }
    }

    override fun printQr(qrData: String) {
        barcodeEncoder.encodeAsBitmap(qrData, BarcodeType.QR_CODE, qrCodeSize.value, qrCodeSize.value)?.let { image ->
            printImage(image)
            printText(singleLineCenteredText(qrData))
        }
    }

    override fun feedPaper() {
        batchSB.append(feedString.value)
    }

    override fun start() {
        if (batchSB.isNotEmpty()) {
            printContent(transformToHtml(batchSB.toString(), includeStyleTag))
            batchSB.clear()
        }
    }
}