package de.tillhub.printengine.verifone

import android.graphics.Bitmap
import com.verifone.peripherals.DirectPrintManager
import com.verifone.peripherals.Printer
import de.tillhub.printengine.HtmlUtils.FEED_PAPER
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

internal class VerifonePrintController(
    private val printManager: DirectPrintManager,
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
    includeStyleTag = false,
    feedString = FEED_PAPER
) {
    private var useCutter = false

    private val printListener: DirectPrintManager.DirectPrintListener by lazy {
        object : DirectPrintManager.DirectPrintListener() {
            /** Called when a print job has moved from the queue and is being processed. */
            override fun started(printId: String?) {
                printerState.value = PrinterState.Busy
            }

            /** Called when the print job cannot continue, but could be resumed later. */
            override fun block(printId: String?, errorMessage: String?) {
                printerState.value = PrinterState.Error.PrintingUnfinished
            }

            /** Called when the print job has finished being cancelled. This is the final message. */
            override fun cancel(printId: String?) {
                printerState.value = PrinterState.Error.PrintingUnfinished
            }

            /** Called when the print job has failed, and cannot be resumed. This is the final message. */
            override fun failed(printId: String?, errorMessage: String?, status: Int) {
                printerState.value = VerifonePrinterState.convert(status)
            }

            /** Called when the print job is complete. */
            override fun complete(printId: String?) {
                printerState.value = PrinterState.Connected
            }
        }
    }

    override fun printContent(content: String, cutAfterPrint: Boolean) {
        printManager.printString(
            printListener,
            transformToHtml(batchSB.toString()),
            when {
                cutAfterPrint || useCutter -> Printer.PRINTER_FULL_CUT
                else -> Printer.PRINTER_NO_CUTTER_LINE_FEED
            }
        )

        useCutter = false
    }

    override fun setFontSize(fontSize: PrintingFontType) = Unit // Not supported

    override fun printImage(image: Bitmap) {
        if (batchPrint) {
            batchSB.append(generateImageHtml(image))
        } else {
            printManager.printBitmap(printListener, image, Printer.PRINTER_NO_CUTTER_LINE_FEED)
        }
    }

    override fun cutPaper() {
        if (batchPrint) {
            useCutter = true
        } else {
            printManager.printString(printListener, "", Printer.PRINTER_FULL_CUT)
        }
    }

    override fun setIntensity(intensity: PrintingIntensity) = Unit // Not supported

    override suspend fun getPrinterInfo(): PrinterInfo =
        PrinterInfo(
            serialNumber = "n/a",
            deviceModel = "Verifone T630c",
            printerVersion = "n/a",
            printerPaperSpec = PrintingPaperSpec.VERIFONE_PAPER_58MM,
            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
            printerHead = "n/a",
            printedDistance = 0,
            serviceVersion = PrinterServiceVersion.Unknown
        )

    companion object {
        private const val BATCH_PRINT_DEFAULT = true
        private const val BARCODE_HEIGHT = 140
        private const val BARCODE_WIDTH = 420
        private const val QR_CODE_SIZE = 420
        private const val FONT_SIZE = 20
    }
}
