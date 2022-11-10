package de.tillhub.printengine.sunmi

import android.content.Context
import android.graphics.Bitmap
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.Printer
import de.tillhub.printengine.analytics.PrintAnalytics
import de.tillhub.printengine.data.*
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

/**
 * Implementation of a Sunmi [Printer].
 */
class SunmiPrinter(
    private val printerService: PrintService = SunmiPrintService(),
    private val analytics: PrintAnalytics
) : Printer {

    private var enabled: Boolean = true

    override fun connect(context: Context) {
        printerService.initPrinterService(context)
    }

    override fun observeConnection(): Flow<PrinterConnectionState> = printerService.printerConnectionState

    override fun enable() {
        enabled = true
    }

    override fun disable() {
        enabled = false
    }

    override suspend fun getPrinterState(): PrinterState =
        printerService.withPrinterOrDefault(default = PrinterState.Error.Unknown) {
            it.getPrinterState()
        }

    override suspend fun getPrinterInfo(): PrinterResult<PrinterInfo> =
        printerService.withPrinterCatching {
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
        printerService.withPrinterCatching {
            logInfo(
                """receipt: START #################
                   |$text
                   |receipt END #################
                   |""".trimMargin()
            )
            if (enabled) {
                it.setFontSize(it.getPrinterInfo().printingFontType)
                it.printText(text)
            }
        }.doOnError {
            logWarning("printing text '$text'")
        }

    override suspend fun printReceipt(text: String, headerImage: Bitmap?): PrinterResult<Unit> =
        printerService.withPrinterCatching {
            logInfo(
                """receipt: START #################
                   |$text
                   |receipt END #################
                   |""".trimMargin()
            )
            if (enabled) {
                it.setFontSize(it.getPrinterInfo().printingFontType)
                if (headerImage != null) it.printImage(headerImage)
                it.printText(text)
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
        signatureQr: String?,
    ): PrinterResult<Unit> =
        printerService.withPrinterCatching {
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
                it.setFontSize(it.getPrinterInfo().printingFontType)
                if (headerImage != null) it.printImage(headerImage)
                it.printText(rawReceiptText)
                signatureQr?.let { qrData ->
                    it.printQr(qrData)
                }
                it.printBarcode(barcode)
                it.feedPaper()
                analytics.logPrintReceipt(rawReceiptText)
            }
        }.doOnError {
            logWarning("printing receipt '$rawReceiptText'")
            analytics.logErrorPrintReceipt("printing receipt '$rawReceiptText'")
        }

    override suspend fun printReceipt(receipt: RawReceipt): PrinterResult<Unit> =
        printerService.withPrinterCatching {
            logInfo(
                """receipt: START #################
                   |${receipt.rawData.bytes.toString(Charsets.UTF_8)}
                   |receipt END #################
                   |""".trimMargin()
            )
            if (enabled) {
                it.setFontSize(it.getPrinterInfo().printingFontType)
                it.sendRawData(receipt.rawData)
                analytics.logPrintReceipt(receipt.rawData.bytes.toString(Charsets.UTF_8))
            }
        }.doOnError {
            logWarning("printing receipt '${receipt.rawData}'")
            analytics.logErrorPrintReceipt("printing receipt '${receipt.rawData}'")
        }

    override suspend fun feedPaper(): PrinterResult<Unit> =
        printerService.withPrinterCatching {
            if (enabled) {
                it.feedPaper()
            }
        }.doOnError {
            logWarning("feeding paper")
        }

    override suspend fun cutPaper(): PrinterResult<Unit> =
        printerService.withPrinterCatching {
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