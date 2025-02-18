package de.tillhub.printengine.pax

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import de.tillhub.printengine.HtmlUtils.FEED_PAPER_SMALL
import de.tillhub.printengine.HtmlUtils.generateImageHtml
import de.tillhub.printengine.HtmlUtils.monospaceText
import de.tillhub.printengine.HtmlUtils.singleLineCenteredText
import de.tillhub.printengine.HtmlUtils.transformToHtml
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.barcode.BarcodeType
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.data.RawPrinterData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


internal class PaxPrinterController(
    private val context: Context,
    private val printerState: MutableStateFlow<PrinterState>,
    private val barcodeEncoder: BarcodeEncoder,
    /**
     * If this field is set to false each print command is handled separately.
     * If it is set to true the print commands are grouped and handled when start() is called
     */
    private val batchPrint: Boolean = BATCH_PRINT_DEFAULT
) : PrinterController {

    private val batchSB = StringBuilder()
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

    override fun setFontSize(fontSize: PrintingFontType) = Unit // Not supported

    override fun printText(text: String) {
        if (batchPrint) {
            batchSB.appendLine(monospaceText(text, FONT_SIZE))
        } else {
            printContent(transformToHtml(monospaceText(text, FONT_SIZE)))
        }
    }

    override fun printBarcode(barcode: String) {
        barcodeEncoder.encodeAsBitmap(barcode, BarcodeType.CODE_128, BARCODE_WIDTH, BARCODE_HEIGHT)?.let { image ->
            printImage(image)
            printText(singleLineCenteredText(barcode))
        }
    }

    override fun printQr(qrData: String) {
        barcodeEncoder.encodeAsBitmap(
            content = qrData,
            type = BarcodeType.QR_CODE,
            imgWidth = QR_CODE_SIZE,
            imgHeight = QR_CODE_SIZE
        )?.let { image ->
            printImage(image)
            printText(singleLineCenteredText(qrData))
        }
    }

    override fun printImage(image: Bitmap) {
        if (batchPrint) {
            batchSB.append(generateImageHtml(image))
        } else {
            printContent(generateImageHtml(image))
        }
    }

    override fun sendRawData(data: RawPrinterData) {
        printText(data.bytes.toString(Charsets.UTF_8))
    }

    override fun observePrinterState(): StateFlow<PrinterState> = printerState

    override fun feedPaper() {
        if (batchPrint) {
            batchSB.append(FEED_PAPER_SMALL)
        } else {
            printContent(FEED_PAPER_SMALL)
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
     * Start printer and prints data in buffer.This is synchronous interface.
     */
    override fun start() {
        if (batchPrint && batchSB.isNotEmpty()) {
            printerState.value = PrinterState.Busy

            printContent(transformToHtml(batchSB.toString()))
            batchSB.clear()
        }
    }

    /**
     * Gets the real-time state of the printer, which can be used before each printing.
     */
    private fun getPrinterState() {
        val intent = Intent().apply {
            component = ComponentName(PRINTING_PACKAGE, PRINTING_CLASS)
        }

        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                printerState.value = PrinterState.Busy

                val printMessenger = Messenger(service)
                printMessenger.send(Message.obtain(null, STATUS_REQUEST, 0, 0).apply {
                    replyTo = Messenger(PrintResponseHandler(printerState))
                })
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }

        if (!context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            context.unbindService(connection)
            printerState.value = PrinterState.Error.NotAvailable
        }
    }

    private fun printContent(content: String) {
        val intent = Intent().apply {
            component = ComponentName(PRINTING_PACKAGE, PRINTING_CLASS)
        }

        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                printerState.value = PrinterState.Busy

                val printMessenger = Messenger(service)
                printMessenger.send(Message.obtain(null, PRINTING_REQUEST, 0, 0).apply {
                    replyTo = Messenger(PrintResponseHandler(printerState))
                    data = Bundle().apply {
                        putString("html", content)
                        putBoolean("autoCrop", true)
                        putInt("grey", printingIntensity)
                    }
                })
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }

        if (!context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            context.unbindService(connection)
            printerState.value = PrinterState.Error.NotAvailable
        }
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
        private const val FONT_SIZE = 15

        private const val PRINTING_PACKAGE = "de.ccv.payment.printservice"
        private const val PRINTING_CLASS = "de.ccv.payment.printservice.DirectPrintService"
        private const val PRINTING_REQUEST = 1
        private const val STATUS_REQUEST = 2
    }
}

internal class PrintResponseHandler(
    private val printerState: MutableStateFlow<PrinterState>
) : Handler(Looper.getMainLooper()) {
    override fun handleMessage(msg: Message) {
        val status = msg.data.getInt("status", 666)
        if (status == 0) {
            printerState.value = PrinterState.Connected
        } else {
            printerState.value = PaxPrinterState.convert(PaxPrinterState.fromCode(status))
        }
    }
}