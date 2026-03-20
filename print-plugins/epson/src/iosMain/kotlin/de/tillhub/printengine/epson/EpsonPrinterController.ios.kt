package de.tillhub.printengine.epson

import androidx.compose.ui.graphics.ImageBitmap
import com.epson.epos2.Epos2Printer
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.RawPrinterData
import de.tillhub.printengine.data.encodeToPngBytes
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage

@Suppress("TooManyFunctions")
@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
internal actual class EpsonPrinterController(
    private val printerData: ExternalPrinter,
    private val epsonPrinter: Epos2Printer,
    private val printerState: MutableStateFlow<PrinterState>,
) : PrinterController {

    actual override fun sendRawData(data: RawPrinterData) = executeEpsonCommand {
        checkSdkResult(epsonPrinter.addTextAlign(EPOS2_ALIGN_CENTER), "addTextAlign")
        checkSdkResult(epsonPrinter.addCommand(data.bytes.toNSData()), "addCommand")
    }

    actual override fun observePrinterState(): Flow<PrinterState> = printerState

    actual override fun setFontSize(fontSize: PrintingFontType) = executeEpsonCommand {
        checkSdkResult(epsonPrinter.addTextAlign(EPOS2_ALIGN_CENTER), "addTextAlign")
        checkSdkResult(
            epsonPrinter.addTextFont(
                when (fontSize) {
                    PrintingFontType.DEFAULT_FONT_SIZE -> EPOS2_FONT_A
                },
            ),
            "addTextFont",
        )
    }

    actual override fun printText(text: String) = executeEpsonCommand {
        checkSdkResult(epsonPrinter.addTextAlign(EPOS2_ALIGN_CENTER), "addTextAlign")
        checkSdkResult(epsonPrinter.addText("$text\n"), "addText")
    }

    actual override fun printBarcode(barcode: String) = executeEpsonCommand {
        checkSdkResult(epsonPrinter.addTextAlign(EPOS2_ALIGN_CENTER), "addTextAlign")
        // "{B" forces Code Set B — same convention as Android
        checkSdkResult(
            epsonPrinter.addBarcode(
                "{B$barcode",
                EPOS2_BARCODE_CODE128,
                EPOS2_HRI_BELOW,
                EPOS2_FONT_A,
                BARCODE_MODULE_WIDTH.toLong(),
                BARCODE_HEIGHT.toLong(),
            ),
            "addBarcode",
        )
        checkSdkResult(epsonPrinter.addFeedLine(THREE_FEED_LINES.toLong()), "addFeedLine")
    }

    actual override fun printQr(qrData: String) = executeEpsonCommand {
        checkSdkResult(epsonPrinter.addTextAlign(EPOS2_ALIGN_CENTER), "addTextAlign")
        checkSdkResult(
            epsonPrinter.addSymbol(
                qrData,
                EPOS2_SYMBOL_QRCODE_MODEL_1,
                EPOS2_LEVEL_M,
                QR_DIMENSION.toLong(),
                QR_DIMENSION.toLong(),
                EPOS2_PARAM_UNSPECIFIED.toLong(),
            ),
            "addSymbol",
        )
    }

    actual override fun printImage(image: ImageBitmap) = executeEpsonCommand {
        checkSdkResult(epsonPrinter.addTextAlign(EPOS2_ALIGN_CENTER), "addTextAlign")
        val uiImage = image.toUIImage() ?: return@executeEpsonCommand
        checkSdkResult(
            epsonPrinter.addImage(
                uiImage,
                IMAGE_START_XY.toLong(),
                IMAGE_START_XY.toLong(),
                image.width.toLong(),
                image.height.toLong(),
                EPOS2_COLOR_1,
                EPOS2_MODE_MONO,
                EPOS2_HALFTONE_DITHER,
                IMAGE_BRIGHTNESS,
                EPOS2_COMPRESS_AUTO,
            ),
            "addImage",
        )
    }

    actual override fun feedPaper() = executeEpsonCommand {
        checkSdkResult(epsonPrinter.addFeedLine(ONE_FEED_LINE.toLong()), "addFeedLine")
    }

    actual override fun cutPaper() = executeEpsonCommand {
        checkSdkResult(epsonPrinter.addCut(EPOS2_CUT_NO_FEED), "addCut")
    }

    actual override fun setIntensity(intensity: PrintingIntensity) = Unit

    actual override fun start() {
        var connected = false
        executeEpsonCommand {
            val status = epsonPrinter.getStatus()
            if (status?.connection != EPOS2_TRUE) {
                checkSdkResult(
                    epsonPrinter.connect(printerData.getTarget(), EPOS2_PARAM_DEFAULT.toLong()),
                    "connect",
                )
            }
            connected = true
            checkSdkResult(epsonPrinter.sendData(EPOS2_PARAM_DEFAULT.toLong()), "sendData")
        }
        epsonPrinter.clearCommandBuffer()
        if (connected) {
            try {
                epsonPrinter.disconnect()
            } catch (_: Exception) {
                // Disconnect failure is non-critical; connection will be re-established next print
            }
        }
    }

    actual override suspend fun getPrinterInfo(): PrinterInfo = printerData.info

    /**
     * Checks an Epson SDK return code and throws [EpsonSdkException] on failure.
     * iOS SDK methods return int codes (0 = success) rather than throwing exceptions,
     * so we must check every return value explicitly.
     */
    private fun checkSdkResult(result: Int, operation: String) {
        if (result != EPOS2_SUCCESS) {
            throw EpsonSdkException(result, operation)
        }
    }

    private fun executeEpsonCommand(command: () -> Unit) {
        try {
            command.invoke()
        } catch (e: EpsonSdkException) {
            printerState.value = EpsonPrinterErrorState.epsonErrorStatusToState(e.errorCode)
            epsonPrinter.clearCommandBuffer()
        } catch (e: Exception) {
            printerState.value = PrinterState.Error.Epson.InternalError
            epsonPrinter.clearCommandBuffer()
        }
    }

    private fun ExternalPrinter.getTarget() = "${connectionType.value}:$connectionAddress"

    @OptIn(ExperimentalForeignApi::class)
    private fun ByteArray.toNSData(): NSData {
        if (isEmpty()) return NSData()
        return usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ImageBitmap.toUIImage(): UIImage? {
        val pngBytes = encodeToPngBytes() ?: return null
        val nsData = pngBytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = pngBytes.size.toULong())
        }
        return UIImage(data = nsData)
    }

    private companion object {
        const val EPOS2_SUCCESS = 0

        const val QR_DIMENSION = 240
        const val BARCODE_HEIGHT = 100
        const val BARCODE_MODULE_WIDTH = 3
        const val ONE_FEED_LINE = 1
        const val THREE_FEED_LINES = 3
        const val IMAGE_BRIGHTNESS = 1.0
        const val IMAGE_START_XY = 0

        // ePOS2.h enum values
        const val EPOS2_TRUE = 1
        const val EPOS2_PARAM_DEFAULT = -2
        const val EPOS2_PARAM_UNSPECIFIED = -1
        const val EPOS2_ALIGN_CENTER = 1
        const val EPOS2_FONT_A = 0
        const val EPOS2_COLOR_1 = 1
        const val EPOS2_MODE_MONO = 0
        const val EPOS2_HALFTONE_DITHER = 0
        const val EPOS2_COMPRESS_AUTO = 2
        const val EPOS2_BARCODE_CODE128 = 10
        const val EPOS2_HRI_BELOW = 2
        const val EPOS2_SYMBOL_QRCODE_MODEL_1 = 2
        const val EPOS2_LEVEL_M = 10
        const val EPOS2_CUT_NO_FEED = 1
    }
}

/**
 * Exception thrown when an Epson SDK method returns a non-zero error code on iOS.
 * On Android, the SDK throws Epos2Exception directly; on iOS, we must check return values.
 */
internal class EpsonSdkException(
    val errorCode: Int,
    val operation: String,
) : Exception("Epson SDK $operation failed with error code: $errorCode")
