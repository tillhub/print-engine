package de.tillhub.printengine.sunmi

import android.graphics.Bitmap
import android.os.RemoteException
import com.sunmi.peripheral.printer.InnerResultCallback
import com.sunmi.peripheral.printer.SunmiPrinterService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.RawPrinterData
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrintingIntensity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * A wrapper to simplify access and interaction with [SunmiPrinterService].
 */
internal class SunmiPrinterController(
    private val printerService: SunmiPrinterService,
    private val serviceVersion: PrinterServiceVersion,
    private val printerState: MutableStateFlow<PrinterState>
) : PrinterController {

    private val printListener: InnerResultCallback by lazy {
        object : InnerResultCallback() {
            override fun onRunResult(isSuccess: Boolean) {
                printerState.value = PrinterState.Connected
            }

            override fun onReturnString(result: String?) {
                printerState.value = PrinterState.Connected
            }

            override fun onRaiseException(code: Int, msg: String?) {
                printerState.value = getPrinterState(code)
            }

            override fun onPrintResult(code: Int, msg: String?) {
                printerState.value = getPrinterState(code)
            }
        }
    }

    override fun sendRawData(data: RawPrinterData) {
        printerService.sendRAWData(data.bytes, printListener)
    }

    override fun observePrinterState(): StateFlow<PrinterState> = printerState

    override fun setFontSize(fontSize: PrintingFontType) {
        printerService.setFontSize(fontSize.toFloatSize(), printListener)
    }

    override fun printText(text: String) {
        printerService.printText(text, printListener)
        printerService.lineWrap(1, printListener)
    }

    override fun printBarcode(barcode: String) {
        printerService.setAlignment(Alignment.CENTER.value, printListener)
        printerService.printBarCode(
            barcode,
            BarcodeType.CODE128.value,
            BARCODE_HEIGHT,
            BARCODE_WIDTH,
            BarcodeTextPosition.UNDER.value,
            printListener
        )
        printerService.setAlignment(Alignment.LEFT.value, printListener)
        printerService.lineWrap(1, printListener)
    }

    override fun printQr(qrData: String) {
        printerService.setAlignment(Alignment.CENTER.value, printListener)
        printerService.printQRCode(
            qrData,
            QRCodeModuleSize.XXXSMALL.value,
            QRCodeErrorLevel.L.value,
            printListener
        )
        printerService.setAlignment(Alignment.LEFT.value, printListener)
        printerService.lineWrap(1, printListener)
    }

    override fun printImage(image: Bitmap) {
        printerService.setAlignment(Alignment.CENTER.value, printListener)
        printerService.printBitmapCustom(image, ImagePrintingMethod.GRAYSCALE.value, printListener)
        printerService.setAlignment(Alignment.LEFT.value, printListener)
        printerService.lineWrap(1, printListener)
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
            printerService.autoOutPaper(printListener)
        } catch (e: RemoteException) {
            Timber.e(e)
            print3Line()
        }
    }

    /**
     * Paper feed three lines. Not disabled when line spacing is set to 0.
     */
    private fun print3Line() = printerService.lineWrap(THREE_LINES, printListener)

    /**
     * Printer cuts paper and throws exception on machines without a cutter
     */
    override fun cutPaper() {
        try {
            printerService.cutPaper(printListener)
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

    /**
     * Gets the real-time state of the printer, which can be used before each printing.
     */
    @Suppress("ComplexMethod")
    private fun getPrinterState(code: Int): PrinterState =
        SunmiPrinterState.convert(SunmiPrinterState.fromCode(code))

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
    NORMAL(value = 20F)
}

private enum class BarcodeType(val value: Int) {
    UPC_A(value = 0),
    UPC_E(value = 1),
    JAN13(value = 2),
    JAN8(value = 3),
    CODE39(value = 4),
    ITF(value = 5),
    CODABAR(value = 6),
    CODE93(value = 7),
    CODE128(value = 8),
}

private enum class ImagePrintingMethod(val value: Int) {
    DEFAULT(value = 0),
    BLACK_AND_WHITE(value = 1),
    GRAYSCALE(value = 2),
}

private enum class BarcodeTextPosition(val value: Int) {
    NO_TEXT(value = 0),
    ABOVE(value = 1),
    UNDER(value = 2),
    ABOVE_AND_UNDER(value = 3),
}

private enum class QRCodeModuleSize(val value: Int) {
    XXXSMALL(value = 3),
    XXSMALL(value = 4),
    XSMALL(value = 6),
    SMALL(value = 8),
    MEDIUM(value = 10),
    LARGE(value = 12),
    XLARGE(value = 14),
    XXLARGE(value = 16),
}

private enum class QRCodeErrorLevel(val value: Int) {
    L(value = 0),
    M(value = 1),
    Q(value = 2),
    H(value = 3),
}

private enum class Alignment(val value: Int) {
    LEFT(value = 0),
    CENTER(value = 1),
    RIGHT(value = 2),
}
