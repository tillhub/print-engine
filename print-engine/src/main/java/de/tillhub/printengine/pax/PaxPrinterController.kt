package de.tillhub.printengine.pax

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import androidx.core.os.bundleOf
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
    private val messenger: Messenger,
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

    private val responseMessenger = Messenger(PrintResponseHandler(printerState))

    init {
        Message.obtain(null, MSG_STATUS, 0, 0).apply {
            replyTo = responseMessenger
        }.also { message ->
            sendMessage(message)
        }
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
        Message.obtain(null, MSG_PRINT, 0, 0).apply {
            replyTo = responseMessenger
            data = bundleOf(
                "html" to content,
                "autoCrop" to true,
                "grey" to printingIntensity
            )
        }.also { message ->
            printerState.value = PrinterState.Busy
            sendMessage(message)
        }
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

    private fun sendMessage(message: Message) {
        try {
            messenger.send(message)
        } catch (e: RemoteException) {
            printerState.value = PrinterState.Error.ConnectionLost
        }
    }

    internal class PrintResponseHandler(
        private val connectionState: MutableStateFlow<PrinterState>
    ) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val status = msg.data.getInt(PRINTER_STATUS_KEY, PaxPrinterState.NotAvailable.code)
            connectionState.value = PaxPrinterState.convert(PaxPrinterState.fromCode(status))
        }
    }

    companion object {
        private const val PRINTER_STATUS_KEY = "status"
        private const val MSG_PRINT = 1
        private const val MSG_STATUS = 2

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