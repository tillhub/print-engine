package de.tillhub.printengine.star

import android.graphics.Bitmap
import com.starmicronics.stario10.StarIO10Exception
import com.starmicronics.stario10.StarPrinter
import com.starmicronics.stario10.starxpandcommand.DocumentBuilder
import com.starmicronics.stario10.starxpandcommand.PrinterBuilder
import com.starmicronics.stario10.starxpandcommand.StarXpandCommandBuilder
import com.starmicronics.stario10.starxpandcommand.printer.Alignment
import com.starmicronics.stario10.starxpandcommand.printer.BarcodeParameter
import com.starmicronics.stario10.starxpandcommand.printer.BarcodeSymbology
import com.starmicronics.stario10.starxpandcommand.printer.CutType
import com.starmicronics.stario10.starxpandcommand.printer.FontType
import com.starmicronics.stario10.starxpandcommand.printer.ImageParameter
import com.starmicronics.stario10.starxpandcommand.printer.QRCodeLevel
import com.starmicronics.stario10.starxpandcommand.printer.QRCodeParameter
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.data.RawPrinterData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class StarPrinterController(
    private val starPrinter: StarPrinter,
    private val printerState: MutableStateFlow<PrinterState>,
    private val commandBuilderFactory: () -> StarXpandCommandBuilder = { StarXpandCommandBuilder() },
    private val documentBuilderFactory: () -> DocumentBuilder = { DocumentBuilder() },
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private var printerBuilder: PrinterBuilder = PrinterBuilder().styleAlignment(Alignment.Center)
) : PrinterController {

    override fun observePrinterState(): Flow<PrinterState> = printerState

    override fun sendRawData(data: RawPrinterData) {
        scope.launch {
            starPrinter.openAsync().await()
            starPrinter.printRawDataAsync(data.bytes.toList()).await()
            starPrinter.closeAsync().await()
        }
    }

    override fun setFontSize(fontSize: PrintingFontType) {
        when (fontSize) {
            PrintingFontType.DEFAULT_FONT_SIZE -> printerBuilder.styleFont(FontType.A)
        }
    }

    override fun printText(text: String) {
        printerBuilder.actionPrintText(text)
            .styleAlignment(Alignment.Center)
    }

    override fun printBarcode(barcode: String) {
        printerBuilder.actionPrintBarcode(
            BarcodeParameter(barcode, BarcodeSymbology.Code128)
                .setBarDots(BAR_DOTS)
                .setHeight(BARCODE_HEIGHT)
                .setPrintHri(true)
        ).styleAlignment(Alignment.Center)
    }

    override fun printQr(qrData: String) {
        printerBuilder.actionPrintQRCode(
            QRCodeParameter(qrData)
                .setLevel(QRCodeLevel.L)
                .setCellSize(CELL_SIZE)
        ).styleAlignment(Alignment.Center)
    }

    override fun printImage(image: Bitmap) {
        printerBuilder.actionPrintImage(ImageParameter(image, IMAGE_WIDTH))
            .styleAlignment(Alignment.Center)
    }

    override fun start() {
        scope.launch {
            runCatching {
                val commandBuilder = commandBuilderFactory().apply {
                    addDocument(documentBuilderFactory().addPrinter(printerBuilder))
                }
                val commands = commandBuilder.getCommands()

                starPrinter.openAsync().await()
                starPrinter.printAsync(commands).await()

                printerBuilder = PrinterBuilder().styleAlignment(Alignment.Center)
            }.onFailure { exception ->
                printerState.value = StarPrinterErrorState.convert(
                    StarPrinterErrorState.fromCode(
                        (exception as StarIO10Exception).errorCode.value
                    )
                )
            }.also {
                starPrinter.closeAsync().await()
            }
        }
    }

    override fun feedPaper() {
        printerBuilder.actionFeedLine(SINGLE_FEED_LINE)
    }

    override fun cutPaper() {
        printerBuilder.actionCut(CutType.Partial)
    }

    override suspend fun getPrinterInfo(): PrinterInfo =
        PrinterInfo(
            serialNumber = "n/a",
            deviceModel = starPrinter.information?.model?.name ?: "Unknown",
            printerVersion = "n/a",
            printerPaperSpec = PrintingPaperSpec.External(characterCount = 32),
            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
            printerHead = "n/a",
            printedDistance = 0,
            serviceVersion = PrinterServiceVersion.Unknown
        )

    override fun setIntensity(intensity: PrintingIntensity) = Unit

    companion object {
        private const val BARCODE_HEIGHT = 12.0
        private const val SINGLE_FEED_LINE = 1
        private const val IMAGE_WIDTH = 406
        private const val CELL_SIZE = 8
        private const val BAR_DOTS = 3
    }
}
