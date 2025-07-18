package de.tillhub.printengine.epson

import android.graphics.Bitmap
import com.epson.epos2.Epos2Exception
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.RawPrinterData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.epson.epos2.printer.Printer as EpsonPrinter

internal class EpsonPrinterController(
    private val printerData: ExternalPrinter,
    private val printerWrapper: EpsonPrinterWrapper,
    private val printerState: MutableStateFlow<PrinterState>,
) : PrinterController {

    override fun sendRawData(data: RawPrinterData) = executeEpsonCommand {
        printerWrapper.addCommand(data.bytes)
    }

    override fun observePrinterState(): StateFlow<PrinterState> = printerState

    override fun setFontSize(fontSize: PrintingFontType) = executeEpsonCommand {
        printerWrapper.addTextFont(
            when (fontSize) {
                PrintingFontType.DEFAULT_FONT_SIZE -> EpsonPrinter.FONT_A
            }
        )
    }

    override fun printText(text: String) = executeEpsonCommand {
        printerWrapper.addText("$text\n")
    }

    override fun printBarcode(barcode: String) = executeEpsonCommand {
        printerWrapper.addBarcode(
            barcode,
            EpsonPrinter.BARCODE_CODE128,
            EpsonPrinter.HRI_BELOW,
            EpsonPrinter.FONT_A,
            BARCODE_MODULE_WIDTH,
            BARCODE_HEIGHT
        )
    }

    override fun printQr(qrData: String) = executeEpsonCommand {
        printerWrapper.addSymbol(
            qrData,
            EpsonPrinter.SYMBOL_QRCODE_MODEL_1,
            EpsonPrinter.LEVEL_M,
            QR_DIMENSION,
            QR_DIMENSION,
            EpsonPrinter.PARAM_UNSPECIFIED
        )
    }

    override fun printImage(image: Bitmap) = executeEpsonCommand {
        printerWrapper.addImage(
            image,
            IMAGE_START_XY,
            IMAGE_START_XY,
            image.getWidth(),
            image.getHeight(),
            EpsonPrinter.COLOR_1,
            EpsonPrinter.MODE_MONO,
            EpsonPrinter.HALFTONE_DITHER,
            IMAGE_BRIGHTNESS,
            EpsonPrinter.COMPRESS_AUTO
        )
    }

    override fun feedPaper() = executeEpsonCommand {
        printerWrapper.addFeedLine(SINGLE_FEED_LINE)
    }

    override fun cutPaper() = executeEpsonCommand {
        printerWrapper.addCut(EpsonPrinter.CUT_NO_FEED)
    }

    override fun setIntensity(intensity: PrintingIntensity) = Unit

    override fun start() {
        executeEpsonCommand {
            if (printerWrapper.status.connection != EpsonPrinter.TRUE) {
                printerWrapper.connect(printerData.getTarget(), EpsonPrinter.PARAM_DEFAULT)
            }
            printerWrapper.sendData(EpsonPrinter.PARAM_DEFAULT)
        }
        printerWrapper.clearCommandBuffer()
        printerWrapper.disconnect()
    }

    private fun executeEpsonCommand(command: () -> Unit) {
        try {
            command.invoke()
        } catch (e: Epos2Exception) {
            printerState.value = EpsonPrinterErrorState.epsonExceptionToState(e)
            printerWrapper.clearCommandBuffer()
        }
    }

    private fun ExternalPrinter.getTarget() =
        "${connectionType.value}:$connectionAddress"

    override suspend fun getPrinterInfo(): PrinterInfo = printerData.info

    companion object {
        private const val QR_DIMENSION = 240
        private const val BARCODE_HEIGHT = 200
        private const val BARCODE_MODULE_WIDTH = 4
        private const val SINGLE_FEED_LINE = 1
        private const val IMAGE_BRIGHTNESS = 1.0
        private const val IMAGE_START_XY = 0
    }
}