package de.tillhub.printengine.verifone

import android.graphics.Bitmap
import com.verifone.peripherals.DirectPrintManager
import com.verifone.peripherals.Printer
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.data.RawPrinterData
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.barcode.BarcodeType
import de.tillhub.printengine.verifone.VerifoneUtils.FEED_PAPER
import de.tillhub.printengine.verifone.VerifoneUtils.generateImageHtml
import de.tillhub.printengine.verifone.VerifoneUtils.monospaceText
import de.tillhub.printengine.verifone.VerifoneUtils.singleLineCenteredText
import de.tillhub.printengine.verifone.VerifoneUtils.transformToHtml
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VerifonePrintController(
    private val printManager: DirectPrintManager,
    private val printerState: MutableStateFlow<PrinterState>,
    private val barcodeEncoder: BarcodeEncoder,
    /**
     * If this field is set to false each print command is handled separately.
     * If it is set to true the print commands are grouped and handled when start() is called
     */
    private val batchPrint: Boolean = BATCH_PRINT_DEFAULT
) : PrinterController {

    private val batchSB = StringBuilder()
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

    override fun sendRawData(data: RawPrinterData) {
        printText(data.bytes.toString(Charsets.UTF_8))
    }

    override fun observePrinterState(): StateFlow<PrinterState> = printerState

    override fun setFontSize(fontSize: PrintingFontType) = Unit // Not supported

    override fun printText(text: String) {
        if (batchPrint) {
            batchSB.appendLine(monospaceText(text))
        } else {
            printManager.printString(
                printListener,
                transformToHtml(monospaceText(text)),
                Printer.PRINTER_NO_CUTTER_LINE_FEED
            )
        }
    }

    override fun printBarcode(barcode: String) {
        barcodeEncoder.encodeAsBitmap(barcode, BarcodeType.CODE_128, BARCODE_WIDTH, BARCODE_HEIGHT)?.let { image ->
            printImage(image)
            printText(singleLineCenteredText(barcode))
        }
    }

    override fun printQr(qrData: String) {
        barcodeEncoder.encodeAsBitmap(qrData, BarcodeType.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE)?.let { image ->
            printImage(image)
            printText(singleLineCenteredText(qrData))
        }
    }

    override fun printImage(image: Bitmap) {
        if (batchPrint) {
            batchSB.append(generateImageHtml(image))
        } else {
            printManager.printBitmap(printListener, image, Printer.PRINTER_NO_CUTTER_LINE_FEED)
        }
    }

    override fun feedPaper() {
        if (batchPrint) {
            batchSB.append(FEED_PAPER)
        } else {
            printManager.printString(printListener, "", Printer.PRINTER_NO_CUT)
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

    override fun start() {
        if (batchPrint && batchSB.isNotEmpty()) {
            printManager.printString(
                printListener,
                transformToHtml(batchSB.toString()),
                if (useCutter) Printer.PRINTER_FULL_CUT else Printer.PRINTER_NO_CUTTER_LINE_FEED
            )

            batchSB.clear()
            useCutter = false
        }
    }

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
    }
}
