package de.tillhub.printengine.pax

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.os.Message
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.html.HtmlPrinterController
import de.tillhub.printengine.html.HtmlUtils.FEED_PAPER_SMALL
import de.tillhub.printengine.html.HtmlUtils.generateImageHtml
import de.tillhub.printengine.html.HtmlUtils.transformToHtml
import kotlinx.coroutines.flow.MutableStateFlow

internal class PaxPrinterController(
    private val paxPrinterConnector: PaxPrinterConnector,
    private val printerState: MutableStateFlow<PrinterState>,
    barcodeEncoder: BarcodeEncoder,
    /**
     * If this field is set to false each print command is handled separately.
     * If it is set to true the print commands are grouped and handled when start() is called
     */
    batchPrint: Boolean = BATCH_PRINT_DEFAULT
) : HtmlPrinterController(
    printerState = printerState,
    barcodeEncoder = barcodeEncoder,
    batchPrint = batchPrint,
    barcodeWidth = BARCODE_WIDTH,
    barcodeHeight = BARCODE_HEIGHT,
    qrCodeSize = QR_CODE_SIZE,
    fontSize = FONT_SIZE,
    includeStyleTag = true,
    feedString = FEED_PAPER_SMALL
) {

    private var printingIntensity: Int = DEFAULT_INTENSITY

    init {
        getPrinterState()
    }

    override suspend fun getPrinterInfo(): PrinterInfo =
        PrinterInfo(
            serialNumber = "n/a",
            deviceModel = "A920",
            printerVersion = "n/a",
            printerPaperSpec = PrintingPaperSpec.PAX_PAPER_56MM,
            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
            printerHead = "n/a",
            printedDistance = 0,
            serviceVersion = PrinterServiceVersion.Unknown
        )

    override fun printContent(content: String, cutAfterPrint: Boolean) {
        paxPrinterConnector.sendPrintRequest(
            payload = content,
            printingIntensity = printingIntensity,
            responseHandler = PrintResponseHandler(printerState)
        )
    }

    override fun setFontSize(fontSize: PrintingFontType) = Unit // Not supported

    override fun printImage(image: Bitmap) {
        val htmlImage = generateImageHtml(image)

        if (batchPrint) {
            batchSB.append(htmlImage)
        } else {
            printContent(transformToHtml(htmlImage, true))
        }
    }

    override fun cutPaper() = Unit

    /**
     * Sets printing intensity (darkness of the print)
     *  DEFAULT: 50
     *  LIGHT: 25
     *  DARK: 70
     *  DARKER: 85
     *  DARKEST: 100
     */
    override fun setIntensity(intensity: PrintingIntensity) {
        printingIntensity = when (intensity) {
            PrintingIntensity.DEFAULT -> DEFAULT_INTENSITY
            PrintingIntensity.LIGHT -> LIGHT_INTENSITY
            PrintingIntensity.DARK -> DARK_INTENSITY
            PrintingIntensity.DARKER -> DARKER_INTENSITY
            PrintingIntensity.DARKEST -> DARKEST_INTENSITY
        }
    }

    /**
     * Gets the real-time state of the printer, which can be used before each printing.
     */
    private fun getPrinterState() {
        paxPrinterConnector.sendStatusRequest(PrintResponseHandler(printerState))
    }

    companion object {
        private const val DEFAULT_INTENSITY = 50
        private const val LIGHT_INTENSITY = 25
        private const val DARK_INTENSITY = 70
        private const val DARKER_INTENSITY = 85
        private const val DARKEST_INTENSITY = 100

        private const val BATCH_PRINT_DEFAULT = true
        private const val BARCODE_HEIGHT = 70
        private const val BARCODE_WIDTH = 220
        private const val QR_CODE_SIZE = 220
        private const val FONT_SIZE = 13
    }
}

internal class PrintResponseHandler(
    private val printerState: MutableStateFlow<PrinterState>
) : Handler(Looper.getMainLooper()) {
    override fun handleMessage(msg: Message) {
        val status = msg.data.getInt("status", PRINTER_NOT_AVAILABLE_ERROR)
        if (status == 0) {
            printerState.value = PrinterState.Connected
        } else {
            printerState.value = PaxPrinterState.convert(PaxPrinterState.fromCode(status))
        }
    }

    companion object {
        private const val PRINTER_NOT_AVAILABLE_ERROR = 666
    }
}