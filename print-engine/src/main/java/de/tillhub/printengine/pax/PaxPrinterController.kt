package de.tillhub.printengine.pax

import android.os.RemoteException
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.html.BarcodeSize
import de.tillhub.printengine.html.FeedString
import de.tillhub.printengine.html.FontSize
import de.tillhub.printengine.html.HtmlPrinterController
import de.tillhub.printengine.html.QrCodeSize
import kotlinx.coroutines.flow.MutableStateFlow

internal class PaxPrinterController(
    private val printService: DirectPrintService,
    private val printerState: MutableStateFlow<PrinterState>,
    barcodeEncoder: BarcodeEncoder,
) : HtmlPrinterController(
    printerState = printerState,
    barcodeEncoder = barcodeEncoder,
    barcodeSize = BarcodeSize.PAX,
    qrCodeSize = QrCodeSize.PAX,
    fontSize = FontSize.PAX,
    includeStyleTag = true,
    feedString = FeedString.PAX
) {
    private var printingIntensity: Int = DEFAULT_INTENSITY

    private val printListener = object : DirectPrintService.DirectPrintListener {
        override fun onFailed(e: RemoteException) {
            printerState.value = PrinterState.Error.ConnectionLost
        }

        override fun onStatus(status: Int) {
            printerState.value = PaxPrinterState.convert(PaxPrinterState.fromCode(status))
        }
    }

    init {
        printService.checkStatus(printListener)
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
        printService.print(content, printingIntensity, printListener).also {
            printerState.value = PrinterState.Busy
        }
    }

    override fun setFontSize(fontSize: PrintingFontType) = Unit // Not supported

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

    companion object {
        private const val DEFAULT_INTENSITY = 50
        private const val LIGHT_INTENSITY = 25
        private const val DARK_INTENSITY = 70
        private const val DARKER_INTENSITY = 85
        private const val DARKEST_INTENSITY = 100
    }
}