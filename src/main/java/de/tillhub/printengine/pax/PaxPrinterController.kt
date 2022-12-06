package de.tillhub.printengine.pax

import android.graphics.Bitmap
import com.pax.dal.IPrinter
import com.pax.dal.entity.EFontTypeAscii
import com.pax.dal.entity.EFontTypeExtCode
import com.pax.dal.exceptions.PrinterDevException
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.*
import de.tillhub.printengine.pax.barcode.BarcodeEncoder
import de.tillhub.printengine.pax.barcode.BarcodeType

/**
 * A wrapper to simplify access and interaction with [IPrinter].
 */
class PaxPrinterController(
    private val printerService: IPrinter,
    private val barcodeEncoder: BarcodeEncoder
) : PrinterController {

    private var fontSize: PrintingFontType? = null

    init {
        printerService.init()
    }

    /**
     * Gets the real-time state of the printer, which can be used before each printing.
     */
    override fun getPrinterState(): PrinterState = try {
        when (PaxPrinterState.fromCode(printerService.status)) {
            PaxPrinterState.Unknown -> PrinterState.Error.Unknown
            PaxPrinterState.Success -> PrinterState.Connected
            PaxPrinterState.Busy -> PrinterState.Busy
            PaxPrinterState.OutOfPaper -> PrinterState.Error.OutOfPaper
            PaxPrinterState.FormatPrintDataPacketError -> PrinterState.Error.FormatPrintDataPacketError
            PaxPrinterState.Malfunctions -> PrinterState.Error.Malfunctions
            PaxPrinterState.Overheated -> PrinterState.Error.Overheated
            PaxPrinterState.VoltageTooLow -> PrinterState.Error.VoltageTooLow
            PaxPrinterState.PrintingUnfinished -> PrinterState.Error.PrintingUnfinished
            PaxPrinterState.NotInstalledFontLibrary -> PrinterState.Error.NotInstalledFontLibrary
            PaxPrinterState.DataPackageTooLong -> PrinterState.Error.DataPackageTooLong
        }
    } catch (e: PrinterDevException) {
        e.printStackTrace()
        PrinterState.Error.Unknown
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

    override fun setFontSize(fontSize: PrintingFontType) {
        this.fontSize = fontSize
        printerService.fontSet(fontSize.toEFontTypeAscii(), fontSize.toEFontTypeExtCode())
    }

    override fun printText(text: String) {
        val optimizedTextForPrinting = printTextOptimizer(text)

        val chunks = chunkForPrinting(optimizedTextForPrinting)
        if (chunks.size > 1) {
            chunks.forEach { chunk ->
                printerService.printStr(chunk, CHARSET)
                printerService.start()
                printerService.init()
                fontSize?.let {
                    printerService.fontSet(it.toEFontTypeAscii(), it.toEFontTypeExtCode())
                }
            }
        } else {
            printerService.printStr(chunks[0], CHARSET)
        }
    }

    override fun printBarcode(barcode: String) {
        barcodeEncoder.encodeAsBitmap(barcode, BarcodeType.CODE_128, BARCODE_WIDTH, BARCODE_HEIGHT)?.let { image ->
            printerService.step(PAPER_FEEDER_DIVIDER)
            printerService.printBitmap(image)
            printerService.printStr(barcodeEncoder.formatCode(
                content = barcode,
                space = PrintingPaperSpec.PAX_PAPER_56MM.characterCount
            ), CHARSET)
        }
    }

    override fun printQr(qrData: String) {
        barcodeEncoder.encodeAsBitmap(qrData, BarcodeType.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE)?.let { image ->
            printerService.step(PAPER_FEEDER_DIVIDER)
            printerService.printBitmap(image)
            printerService.printStr(barcodeEncoder.formatCode(
                content = qrData,
                space = PrintingPaperSpec.PAX_PAPER_56MM.characterCount
            ), CHARSET)
        }
    }

    override fun printImage(image: Bitmap) {
        printerService.printBitmap(image)
    }

    override fun sendRawData(data: RawPrinterData) {
        printText(data.bytes.toString(Charsets.UTF_8))
    }

    /**
     *  Due to the distance between the paper hatch and the print head, the paper needs to be fed out automatically
     *  But if the Api does not support it, it will be replaced by printing three lines
     */
    override fun feedPaper() {
        printerService.step(PAPER_FEEDER_LENGTH_END)
    }

    /**
     * Printer cuts paper and throws exception on machines without a cutter
     *  0:Only support full paper cut
     *  1:Only support partial paper cutting
     *  2:support partial paper and full paper cutting
     *  -1:No cutting knife,not support
     */
    override fun cutPaper() {
        printerService.cutPaper(FULL_PAPER_CUT)
    }

    /**
     * Sets printing intensity (darkness of the print)
     *  DEFAULT: 100%
     *  LIGHT: 50%
     *  DARK: 150%
     *  DARKER: 250%
     *  DARKEST: 500%
     */
    override fun setIntensity(intensity: PrintingIntensity) {
        printerService.setGray(when (intensity) {
            PrintingIntensity.DEFAULT -> DEFAULT_INTENSITY
            PrintingIntensity.LIGHT -> LIGHT_INTENSITY
            PrintingIntensity.DARK -> DARK_INTENSITY
            PrintingIntensity.DARKER -> DARKER_INTENSITY
            PrintingIntensity.DARKEST -> DARKEST_INTENSITY
        })
    }

    /**
     * Start printer and prints data in buffer.This is synchronous interface.
     */
    override fun start() {
        printerService.start()
        printerService.init()
        fontSize = null
    }

    /**
     * It fixes issues with pax printer printing non ASCII characters.
     */
    private fun printTextOptimizer(text: String): String {
        return text.replace("\\s€".toRegex(), "€")
    }

    private fun chunkForPrinting(text: String): List<String> {
        val lines = text.split('\n')

        return mutableListOf<String>().apply {
            val sb = StringBuilder()
            lines.forEachIndexed { index, s ->
                sb.append(s)
                sb.append("\n")
                if (index == (lines.size - 1) || (index + 1) % MAX_PRINT_LINES == 0) {
                    add(sb.toString())
                    sb.clear()
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_INTENSITY = 1
        private const val LIGHT_INTENSITY = 50
        private const val DARK_INTENSITY = 150
        private const val DARKER_INTENSITY = 250
        private const val DARKEST_INTENSITY = 500

        const val FULL_PAPER_CUT = 0
        const val PAPER_FEEDER_DIVIDER = 20
        const val PAPER_FEEDER_LENGTH_END = 180
        const val CHARSET = "UTF-8"

        private const val BARCODE_HEIGHT = 150
        private const val BARCODE_WIDTH = 500
        private const val QR_CODE_SIZE = 500

        // Max buffer size for printing is 2048 bytes.
        // Max lines * paper width (32) should not go over the buffer size limit
        private const val MAX_PRINT_LINES = 50

        private fun PrintingFontType.toEFontTypeAscii() =
            when (this) {
                PrintingFontType.DEFAULT_FONT_SIZE -> EFontTypeAscii.FONT_12_24
            }

        private fun PrintingFontType.toEFontTypeExtCode() =
            when (this) {
                PrintingFontType.DEFAULT_FONT_SIZE -> EFontTypeExtCode.FONT_16_16
            }
    }
}