package de.tillhub.printengine.html

import de.tillhub.printengine.HtmlUtils.monospaceText
import de.tillhub.printengine.HtmlUtils.singleLineCenteredText
import de.tillhub.printengine.HtmlUtils.transformToHtml
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.barcode.BarcodeType
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.RawPrinterData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal abstract class HtmlPrinterController(
    private val printerState: MutableStateFlow<PrinterState>,
    private val barcodeEncoder: BarcodeEncoder,
    /**
     * If this field is set to false each print command is handled separately.
     * If it is set to true the print commands are grouped and handled when start() is called
     */
    protected val batchPrint: Boolean,
    protected val barcodeWidth: Int,
    protected val barcodeHeight: Int,
    protected val qrCodeSize: Int,
    protected val fontSize: Int,
    protected val includeStyleTag: Boolean,
    protected val feedString: String,
) : PrinterController {
    protected val batchSB = StringBuilder()

    protected abstract fun printContent(content: String, cutAfterPrint: Boolean = false)

    override fun observePrinterState(): StateFlow<PrinterState> = printerState

    override fun sendRawData(data: RawPrinterData) {
        printText(data.bytes.toString(Charsets.UTF_8))
    }

    override fun printText(text: String) {
        if (batchPrint) {
            batchSB.appendLine(monospaceText(text, fontSize))
        } else {
            printContent(transformToHtml(monospaceText(text, fontSize), includeStyleTag))
        }
    }

    override fun printBarcode(barcode: String) {
        barcodeEncoder.encodeAsBitmap(barcode, BarcodeType.CODE_128, barcodeWidth, barcodeHeight)?.let { image ->
            printImage(image)
            printText(singleLineCenteredText(barcode))
        }
    }

    override fun printQr(qrData: String) {
        barcodeEncoder.encodeAsBitmap(qrData, BarcodeType.QR_CODE, qrCodeSize, qrCodeSize)?.let { image ->
            printImage(image)
            printText(singleLineCenteredText(qrData))
        }
    }

    override fun feedPaper() {
        if (batchPrint) {
            batchSB.append(feedString)
        } else {
            printContent(feedString)
        }
    }

    override fun start() {
        if (batchPrint && batchSB.isNotEmpty()) {
            printContent(transformToHtml(batchSB.toString(), includeStyleTag))
            batchSB.clear()
        }
    }
}