package de.tillhub.printengine.html

import androidx.compose.ui.graphics.ImageBitmap
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.barcode.BarcodeType
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.data.RawPrinterData
import de.tillhub.printengine.data.normalizeForPrintHead
import de.tillhub.printengine.html.HtmlUtils.generateImageHtml
import de.tillhub.printengine.html.HtmlUtils.monospaceText
import de.tillhub.printengine.html.HtmlUtils.singleLineCenteredText
import de.tillhub.printengine.html.HtmlUtils.transformToHtml
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

abstract class HtmlPrinterController(
    private val printerState: MutableStateFlow<PrinterState>,
    private val barcodeEncoder: BarcodeEncoder,
    val paperSpec: PrintingPaperSpec,
    val barcodeSize: BarcodeSize,
    val qrCodeSize: QrCodeSize,
    val fontSize: FontSize,
    val includeStyleTag: Boolean,
    val feedString: FeedString,
) : PrinterController {
    private val batchSB = StringBuilder()

    protected abstract fun printContent(
        content: String,
        cutAfterPrint: Boolean = false,
    )

    override fun observePrinterState(): Flow<PrinterState> = printerState

    override fun sendRawData(data: RawPrinterData) {
        printText(data.bytes.decodeToString())
    }

    override fun printText(text: String) {
        batchSB.appendLine(monospaceText(text, fontSize.value))
    }

    /**
     * Normalised for the print head first: inlined into the receipt HTML at full colour and
     * resolution, an image is unprintable detail and big enough to overflow the IPC transaction.
     */
    override fun printImage(image: ImageBitmap) {
        appendImage(image.normalizeForPrintHead(paperSpec.printHeadWidthPx))
    }

    override fun printBarcode(barcode: String) {
        barcodeEncoder
            .encodeAsBitmap(
                content = barcode,
                type = BarcodeType.CODE_128,
                imgWidth = barcodeSize.width,
                imgHeight = barcodeSize.height,
            )?.let { image ->
                appendImage(image)
                printText(singleLineCenteredText(barcode))
            }
    }

    override fun printQr(qrData: String) {
        barcodeEncoder
            .encodeAsBitmap(
                content = qrData,
                type = BarcodeType.QR_CODE,
                imgWidth = qrCodeSize.value,
                imgHeight = qrCodeSize.value,
            )?.let { image ->
                appendImage(image)
                printText(singleLineCenteredText(qrData))
            }
    }

    override fun feedPaper() {
        batchSB.append(feedString.value)
    }

    override fun start() {
        if (batchSB.isEmpty()) return

        val content = transformToHtml(batchSB.toString(), includeStyleTag)
        // Cleared before the content is handed over, so that a receipt the transport rejects
        // cannot leak into the next print job.
        batchSB.clear()

        printContent(content)
    }

    /**
     * Appends an image that is already fit for the print head, skipping normalisation.
     */
    private fun appendImage(image: ImageBitmap) {
        batchSB.append(generateImageHtml(image))
    }
}
