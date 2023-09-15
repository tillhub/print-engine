package de.tillhub.printengine.emulated

import android.content.Context
import android.graphics.Bitmap
import de.tillhub.printengine.Printer
import de.tillhub.printengine.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

/**
 * EmulatedPrinter is used when Printer is not available or supported by device
 */
class EmulatedPrinter : Printer {

    override fun connect(context: Context) {
        logInfo("printer connected")
    }

    override fun setEnabled(enabled: Boolean) {
        if (enabled) logInfo("printer enabled")
        else logInfo("printer disabled")
    }

    override fun observeConnection(): Flow<PrinterConnectionState> = flow {
        emit(PrinterConnectionState.PrinterNotAvailable)
    }

    override fun getPrinterState(): PrinterState = PrinterState.PrinterNotDetected

    override fun setPrintingIntensity(intensity: PrintingIntensity) {
        logInfo("setting printer intensity to: ${intensity.name}")
    }

    override suspend fun getPrinterInfo(): PrinterResult<PrinterInfo> {
        return PrinterResult.Success(PrinterInfo(
            serialNumber = "n/a",
            deviceModel = "Emulated Printer",
            printerVersion = "n/a",
            printerPaperSpec = PrintingPaperSpec.PAX_PAPER_56MM,
            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
            printerHead = "n/a",
            printedDistance = 0,
            serviceVersion = PrinterServiceVersion.Info(
                serviceVersionName = "1.0.0",
                serviceVersionCode = 1
            )
        ))
    }


    override suspend fun printText(text: String): PrinterResult<Unit> {
        logInfo("printing text '$text'")
        return PrinterResult.Success(Unit)
    }

    override suspend fun printReceipt(text: String, headerImage: Bitmap?): PrinterResult<Unit> {
        logInfo(
            """receipt: START #################
                   |$text
                   |receipt END #################
                   |""".trimMargin()
        )
        return PrinterResult.Success(Unit)
    }

    override suspend fun printReceipt(
        rawReceiptText: String,
        barcode: String,
        headerImage: Bitmap?,
        footerImage: Bitmap?,
        signatureQr: String?
    ): PrinterResult<Unit> {
        logInfo(
            """receipt: START #################
                   |$rawReceiptText
                   |receipt SIGNATURE QR #################
                   |$signatureQr
                   |receipt BARCODE #################
                   |$barcode
                   |receipt END #################
                   |""".trimMargin()
        )
        return PrinterResult.Success(Unit)
    }

    override suspend fun printReceipt(receipt: RawReceipt): PrinterResult<Unit> {
        logInfo(
            """receipt: START #################
                   |${receipt.rawData.bytes.toString(Charsets.UTF_8)}
                   |receipt END #################
                   |""".trimMargin()
        )
        return PrinterResult.Success(Unit)
    }

    override suspend fun feedPaper(): PrinterResult<Unit> {
        logInfo("feeding paper")
        return PrinterResult.Success(Unit)
    }

    override suspend fun cutPaper(): PrinterResult<Unit> {
        logInfo("cut paper")
        return PrinterResult.Success(Unit)
    }

    private fun logInfo(message: String) {
        Timber.i("EmulatedPrinter: $message")
    }
}