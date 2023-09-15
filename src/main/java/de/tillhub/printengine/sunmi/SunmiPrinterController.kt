package de.tillhub.printengine.sunmi

import android.graphics.Bitmap
import android.os.RemoteException
import com.sunmi.peripheral.printer.SunmiPrinterService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.RawPrinterData
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrintingIntensity
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * A wrapper to simplify access and interaction with [SunmiPrinterService].
 */
class SunmiPrinterController(
    private val printerService: SunmiPrinterService,
    private val serviceVersion: PrinterServiceVersion
) : PrinterController {

    override fun sendRawData(data: RawPrinterData) {
        printerService.sendRAWData(data.bytes, null)
    }

    /**
     * Gets the real-time state of the printer, which can be used before each printing.
     */
    @Suppress("ComplexMethod")
    override fun getPrinterState(): PrinterState = try {
        printerService.updatePrinterState().let { when (SunmiPrinterState.fromCode(it)) {
            SunmiPrinterState.Unknown -> PrinterState.Error.Unknown
            SunmiPrinterState.Connected -> PrinterState.Connected
            SunmiPrinterState.Preparing -> PrinterState.Preparing
            SunmiPrinterState.AbnormalCommunication -> PrinterState.Error.AbnormalCommunication
            SunmiPrinterState.OutOfPaper -> PrinterState.Error.OutOfPaper
            SunmiPrinterState.Overheated -> PrinterState.Error.Overheated
            SunmiPrinterState.CoverNotClosed -> PrinterState.Error.CoverNotClosed
            SunmiPrinterState.PaperCutterAbnormal -> PrinterState.Error.PaperCutterAbnormal
            SunmiPrinterState.PaperCutterRecovered -> PrinterState.Connected
            SunmiPrinterState.BlackMarkNotFound -> PrinterState.Error.BlackMarkNotFound
            SunmiPrinterState.NotDetected -> PrinterState.PrinterNotDetected
            SunmiPrinterState.FirmwareUpgradeFailed -> PrinterState.Error.FirmwareUpgradeFailed
        } }
    } catch (e: RemoteException) {
        Timber.e(e)
        PrinterState.Error.Unknown
    }

    override fun setFontSize(fontSize: PrintingFontType) {
        printerService.setFontSize(fontSize.toFloatSize(), null)
    }

    override fun printText(text: String) {
        printerService.printText(text, null)
        printerService.lineWrap(1, null)
    }

    override fun printBarcode(barcode: String) {
        printerService.setAlignment(Alignment.CENTER.value, null)
        printerService.printBarCode(
            barcode,
            BarcodeType.CODE128.value,
            BARCODE_HEIGHT,
            BARCODE_WIDTH,
            BarcodeTextPosition.UNDER.value,
            null
        )
        printerService.setAlignment(Alignment.LEFT.value, null)
        printerService.lineWrap(1, null)
    }

    override fun printQr(qrData: String) {
        printerService.setAlignment(Alignment.CENTER.value, null)
        printerService.printQRCode(
            qrData,
            QRCodeModuleSize.XXXSMALL.value,
            QRCodeErrorLevel.L.value,
            null
        )
        printerService.setAlignment(Alignment.LEFT.value, null)
        printerService.lineWrap(1, null)
    }

    override fun printImage(image: Bitmap) {
        printerService.setAlignment(Alignment.CENTER.value, null)
        printerService.printBitmapCustom(image, ImagePrintingMethod.GRAYSCALE.value, null)
        printerService.setAlignment(Alignment.LEFT.value, null)
        printerService.lineWrap(1, null)
    }

    override suspend fun getPrinterInfo(): PrinterInfo = printerService.let {
        PrinterInfo(
            it.printerSerialNo,
            it.printerModal,
            it.printerVersion,
            it.printerPaper.toPrintingPaperSpec(),
            PrintingFontType.DEFAULT_FONT_SIZE,
            it.loadPrinterFactory(),
            it.loadPrintedLength(),
            serviceVersion
        )
    }

    private suspend fun SunmiPrinterService.loadPrinterFactory(): String =
        suspendCoroutine { continuation ->
            getPrinterFactory(object : SunmiInnerResultCallback() {
                override fun onReturnString(result: String?) {
                    continuation.resume(result.orEmpty())
                }

                override fun onRaiseException(code: Int, msg: String?) {
                    continuation.resume("")
                }
            })
        }

    private suspend fun SunmiPrinterService.loadPrintedLength(): Int =
        suspendCoroutine { continuation ->
            getPrintedLength(object : SunmiInnerResultCallback() {
                override fun onReturnString(result: String?) {
                    continuation.resume(result?.toIntOrNull() ?: 0)
                }

                override fun onRaiseException(code: Int, msg: String?) {
                    continuation.resume(0)
                }
            })
        }

    /**
     *  Due to the distance between the paper hatch and the print head, the paper needs to be fed out automatically
     *  But if the Api does not support it, it will be replaced by printing three lines
     */
    override fun feedPaper() {
        try {
            printerService.autoOutPaper(null)
        } catch (e: RemoteException) {
            Timber.e(e)
            print3Line()
        }
    }

    /**
     * Paper feed three lines. Not disabled when line spacing is set to 0.
     */
    private fun print3Line() = printerService.lineWrap(THREE_LINES, null)

    /**
     * Printer cuts paper and throws exception on machines without a cutter
     */
    override fun cutPaper() {
        try {
            printerService.cutPaper(null)
        } catch (e: RemoteException) {
            Timber.e(e)
            // not handled
        }
    }

    override fun setIntensity(intensity: PrintingIntensity) {
        // not needed for sunmi devices
    }

    override fun start() {
        // not needed for sunmi devices
    }

    companion object {
        const val THREE_LINES = 3

        private const val BARCODE_HEIGHT = 100
        private const val BARCODE_WIDTH = 2 // default sunmi value
    }
}

private fun Int.toPrintingPaperSpec(): PrintingPaperSpec = PrintingPaperSpec.SUNMI_PAPER_56MM

private fun PrintingFontType.toFloatSize() = when (this) {
    PrintingFontType.DEFAULT_FONT_SIZE -> FontSize.NORMAL.value
}

private enum class FontSize(val value: Float) {
    NORMAL(20F)
}

private enum class BarcodeType(val value: Int) {
    UPC_A(0),
    UPC_E(1),
    JAN13(2),
    JAN8(3),
    CODE39(4),
    ITF(5),
    CODABAR(6),
    CODE93(7),
    CODE128(8),
}

private enum class ImagePrintingMethod(val value: Int) {
    DEFAULT(0),
    BLACK_AND_WHITE(1),
    GRAYSCALE(2),
}

private enum class BarcodeTextPosition(val value: Int) {
    NO_TEXT(0),
    ABOVE(1),
    UNDER(2),
    ABOVE_AND_UNDER(3),
}

private enum class QRCodeModuleSize(val value: Int) {
    XXXSMALL(3),
    XXSMALL(4),
    XSMALL(6),
    SMALL(8),
    MEDIUM(10),
    LARGE(12),
    XLARGE(14),
    XXLARGE(16),
}

private enum class QRCodeErrorLevel(val value: Int) {
    L(0),
    M(1),
    Q(2),
    H(3),
}

private enum class Alignment(val value: Int) {
    LEFT(0),
    CENTER(1),
    RIGHT(2),
}