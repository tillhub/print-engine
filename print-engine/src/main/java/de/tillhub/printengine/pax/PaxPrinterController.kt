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
import de.tillhub.printengine.HtmlUtils.transformToHtml
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.html.HtmlPrinterController
import kotlinx.coroutines.flow.MutableStateFlow

internal class PaxPrinterController(
    private val context: Context,
    private val printerState: MutableStateFlow<PrinterState>,
    barcodeEncoder: BarcodeEncoder,
) : HtmlPrinterController(
    printerState = printerState,
    barcodeEncoder = barcodeEncoder,
    batchPrint = BATCH_PRINT_DEFAULT,
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

    override fun setFontSize(fontSize: PrintingFontType) = Unit // Not supported

    override fun printImage(image: Bitmap) {
        val htmlImage = generateImageHtml(image)

        if (batchPrint) {
            batchSB.append(htmlImage)
        } else {
            printContent(htmlImage)
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