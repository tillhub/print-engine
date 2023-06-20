package de.tillhub.printengine.pax

import android.content.Context
import android.graphics.Bitmap
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.Printer
import de.tillhub.printengine.analytics.PrintAnalytics
import de.tillhub.printengine.data.*
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

/**
 * Implementation of a Pax A920 [Printer].
 */
class PaxPrinter(
    private val paxPrintService: PrintService = PaxPrintService(),
    private val analytics: PrintAnalytics
) : Printer {

    private var enabled: Boolean = true
    private var printingIntensity: PrintingIntensity = PrintingIntensity.DEFAULT

    override fun connect(context: Context) {
        paxPrintService.initPrinterService(context)
    }

    override fun observeConnection(): Flow<PrinterConnectionState> = paxPrintService.printerConnectionState

    override fun enable() {
        enabled = true
    }

    override fun disable() {
        enabled = false
    }

    override fun getPrinterState(): PrinterState =
        paxPrintService.withPrinterOrDefault(default = PrinterState.Error.Unknown) {
            it.getPrinterState()
        }

    override fun setPrintingIntensity(intensity: PrintingIntensity) {
        printingIntensity = intensity
    }

    override suspend fun getPrinterInfo(): PrinterResult<PrinterInfo> =
        paxPrintService.withPrinterCatching {
            it.getPrinterInfo().let { info ->
                PrinterInfo(
                    info.serialNumber,
                    info.deviceModel,
                    info.printerVersion,
                    info.printerPaperSpec,
                    info.printingFontType,
                    info.printerHead,
                    info.printedDistance,
                    info.serviceVersion
                )
            }
        }.doOnError {
            logWarning("getting printer info")
        }

    override suspend fun printText(text: String): PrinterResult<Unit> =
        paxPrintService.withPrinterCatching {
            logInfo(
                """receipt: START #################
                   |$text
                   |receipt END #################
                   |""".trimMargin()
            )
            if (enabled) {
                it.setIntensity(printingIntensity)
                it.setFontSize(it.getPrinterInfo().printingFontType)
                it.printText(text)
                it.feedPaper()
                it.start()
            }
        }.doOnError {
            logWarning("printing text '$text'")
        }

    override suspend fun printReceipt(text: String, headerImage: Bitmap?): PrinterResult<Unit> =
        paxPrintService.withPrinterCatching { printer ->
            logInfo(
                """receipt: START #################
                   |$text
                   |receipt END #################
                   |""".trimMargin()
            )
            if (enabled) {
                printer.setIntensity(printingIntensity)
                printer.setFontSize(printer.getPrinterInfo().printingFontType)
                headerImage?.let { printer.printImage(it) }
                printer.printText(text)
                printer.feedPaper()
                printer.start()
                analytics.logPrintReceipt(text)
            }
        }.doOnError {
            logWarning("printing text '$text'")
            analytics.logErrorPrintReceipt("printing text '$text'")
        }

    override suspend fun printReceipt(
        rawReceiptText: String,
        barcode: String,
        headerImage: Bitmap?,
        footerImage: Bitmap?,
        signatureQr: String?
    ): PrinterResult<Unit> =
        paxPrintService.withPrinterCatching { printer ->
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
            if (enabled) {
                printer.setIntensity(printingIntensity)
                printer.setFontSize(printer.getPrinterInfo().printingFontType)
                headerImage?.let { printer.printImage(it) }
                printer.printText(rawReceiptText)
                signatureQr?.let { printer.printQr(it) }
                footerImage?.let { printer.printImage(it) }
                printer.printBarcode(barcode)
                printer.feedPaper()
                printer.start()
                analytics.logPrintReceipt(rawReceiptText)
            }
        }.doOnError {
            logWarning("printing receipt '$rawReceiptText'")
            analytics.logErrorPrintReceipt("printing receipt '$rawReceiptText'")
        }

    override suspend fun printReceipt(receipt: RawReceipt): PrinterResult<Unit> =
        paxPrintService.withPrinterCatching { printer ->
            logInfo(
                """receipt: START #################
                   |${receipt.rawData.bytes.toString(Charsets.UTF_8)}
                   |receipt END #################
                   |""".trimMargin()
            )
            if (enabled) {
                printer.setIntensity(printingIntensity)
                printer.setFontSize(printer.getPrinterInfo().printingFontType)
                printer.sendRawData(receipt.rawData)
                printer.feedPaper()
                printer.start()
                analytics.logPrintReceipt(receipt.rawData.bytes.toString(Charsets.UTF_8))
            }
        }.doOnError {
            logWarning("printing receipt '${receipt.rawData}'")
            analytics.logErrorPrintReceipt("printing receipt '${receipt.rawData}'")
        }

    override suspend fun feedPaper(): PrinterResult<Unit> =
        paxPrintService.withPrinterCatching {
            if (enabled) {
                it.feedPaper()
            }
        }.doOnError {
            logWarning("feeding paper")
        }

    override suspend fun cutPaper(): PrinterResult<Unit> =
        paxPrintService.withPrinterCatching {
            if (enabled) {
                it.cutPaper()
            }
        }.doOnError {
            logWarning("cutting paper")
        }

    private fun logInfo(message: String) {
        Timber.i("printing: $message")
    }

    private fun logWarning(reason: String) {
        Timber.w("Printer not connected for: $reason")
    }
}