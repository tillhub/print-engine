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
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.skia.Bitmap as SkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image as SkiaImage
import org.jetbrains.skia.ImageInfo
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.posix.memcpy

@Suppress("TooManyFunctions")
@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
internal actual class EpsonPrinterController(
    private val printerData: ExternalPrinter,
    private val epsonPrinter: Epos2Printer,
    private val printerState: MutableStateFlow<PrinterState>,
) : PrinterController {

    actual override fun sendRawData(data: RawPrinterData) = executeEpsonCommand {
        epsonPrinter.addTextAlign(EPOS2_ALIGN_CENTER)
        epsonPrinter.addCommand(data.bytes.toNSData())
    }

    actual override fun observePrinterState(): Flow<PrinterState> = printerState

    actual override fun setFontSize(fontSize: PrintingFontType) = executeEpsonCommand {
        epsonPrinter.addTextAlign(EPOS2_ALIGN_CENTER)
        epsonPrinter.addTextFont(
            when (fontSize) {
                PrintingFontType.DEFAULT_FONT_SIZE -> EPOS2_FONT_A
            },
        )
    }

    actual override fun printText(text: String) = executeEpsonCommand {
        epsonPrinter.addTextAlign(EPOS2_ALIGN_CENTER)
        epsonPrinter.addText("$text\n")
    }

    actual override fun printBarcode(barcode: String) = executeEpsonCommand {
        epsonPrinter.addTextAlign(EPOS2_ALIGN_CENTER)
        // "{B" forces Code Set B — same convention as Android
        epsonPrinter.addBarcode(
            "{B$barcode",
            EPOS2_BARCODE_CODE128,
            EPOS2_HRI_BELOW,
            EPOS2_FONT_A,
            BARCODE_MODULE_WIDTH.toLong(),
            BARCODE_HEIGHT.toLong(),
        )
        epsonPrinter.addFeedLine(THREE_FEED_LINES.toLong())
    }

    actual override fun printQr(qrData: String) = executeEpsonCommand {
        epsonPrinter.addTextAlign(EPOS2_ALIGN_CENTER)
        epsonPrinter.addSymbol(
            qrData,
            EPOS2_SYMBOL_QRCODE_MODEL_1,
            EPOS2_LEVEL_M,
            QR_DIMENSION.toLong(),
            QR_DIMENSION.toLong(),
            EPOS2_PARAM_UNSPECIFIED.toLong(),
        )
    }

    actual override fun printImage(image: ImageBitmap) = executeEpsonCommand {
        epsonPrinter.addTextAlign(EPOS2_ALIGN_CENTER)
        val uiImage = image.toUIImage() ?: return@executeEpsonCommand
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
        )
    }

    actual override fun feedPaper() = executeEpsonCommand {
        epsonPrinter.addFeedLine(ONE_FEED_LINE.toLong())
    }

    actual override fun cutPaper() = executeEpsonCommand {
        epsonPrinter.addCut(EPOS2_CUT_NO_FEED)
    }

    actual override fun setIntensity(intensity: PrintingIntensity) = Unit

    actual override fun start() {
        executeEpsonCommand {
            val status = epsonPrinter.getStatus()
            if (status?.connection != EPOS2_TRUE) {
                epsonPrinter.connect(printerData.getTarget(), EPOS2_PARAM_DEFAULT.toLong())
            }
            epsonPrinter.addTextAlign(EPOS2_ALIGN_CENTER)
            epsonPrinter.sendData(EPOS2_PARAM_DEFAULT.toLong())
        }
        epsonPrinter.clearCommandBuffer()
        epsonPrinter.disconnect()
    }

    actual override suspend fun getPrinterInfo(): PrinterInfo = printerData.info

    private fun executeEpsonCommand(command: () -> Unit) {
        try {
            command.invoke()
        } catch (e: Exception) {
            printerState.value = EpsonPrinterErrorState.epsonExceptionToState(e)
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
        val w = width
        val h = height
        val buffer = IntArray(w * h)
        readPixels(buffer)

        val bytes = ByteArray(buffer.size * 4)
        buffer.usePinned { src ->
            bytes.usePinned { dst ->
                memcpy(dst.addressOf(0), src.addressOf(0), bytes.size.toULong())
            }
        }

        val imageInfo = ImageInfo.makeN32Premul(w, h)
        val skiaBitmap = SkiaBitmap().apply {
            allocPixels(imageInfo)
            installPixels(imageInfo, bytes, w * 4)
        }
        val skiaImage = SkiaImage.makeFromBitmap(skiaBitmap)
        val pngData = skiaImage.encodeToData(EncodedImageFormat.PNG) ?: return null

        val nsData = pngData.bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = pngData.bytes.size.toULong())
        }
        return UIImage(data = nsData)
    }

    private companion object {
        const val QR_DIMENSION          = 240
        const val BARCODE_HEIGHT        = 100
        const val BARCODE_MODULE_WIDTH  = 3
        const val ONE_FEED_LINE         = 1
        const val THREE_FEED_LINES      = 3
        const val IMAGE_BRIGHTNESS      = 1.0
        const val IMAGE_START_XY        = 0

        // Constants matching ePOS2.h enum values
        const val EPOS2_TRUE            = 1
        const val EPOS2_PARAM_DEFAULT   = -2
        const val EPOS2_PARAM_UNSPECIFIED = -1
        const val EPOS2_ALIGN_CENTER    = 1
        const val EPOS2_FONT_A          = 0
        const val EPOS2_COLOR_1         = 1
        const val EPOS2_MODE_MONO       = 0
        const val EPOS2_HALFTONE_DITHER = 0
        const val EPOS2_COMPRESS_AUTO   = 2
        const val EPOS2_BARCODE_CODE128 = 10
        const val EPOS2_HRI_BELOW       = 2
        const val EPOS2_SYMBOL_QRCODE_MODEL_1 = 2  // EPOS2_SYMBOL_QRCODE_MODEL_1 (PDF417_STANDARD=0, PDF417_TRUNCATED=1, QRCODE_MODEL_1=2)
        const val EPOS2_LEVEL_M         = 10 // EPOS2_LEVEL_M (LEVEL_0..LEVEL_8=0..8, LEVEL_L=9, LEVEL_M=10)
        const val EPOS2_CUT_NO_FEED     = 1
    }
}
