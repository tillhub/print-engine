package de.tillhub.printengine.sunmi

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
    private val printService: PrintService,
    private val analytics: PrintAnalytics?
) : Printer {

    private var enabled: Boolean = true

    override fun observeConnection(): Flow<PrinterConnectionState> = printService.printerConnectionState

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun getPrinterState(): PrinterState =
        printService.withPrinterOrDefault(default = PrinterState.Error.Unknown) {
            it.getPrinterState()
        }

    override fun setPrintingIntensity(intensity: PrintingIntensity) {
        // not needed for sunmi devices
    }

    override suspend fun getPrinterInfo(): PrinterResult<PrinterInfo> =
        printService.withPrinterCatching {
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

    override suspend fun startPrintJob(job: PrintJob): PrinterResult<Unit> =
        printService.withPrinterCatching { controller ->
            logInfo(
                """receipt: START #################
                   |${job.description}
                   |receipt END #################
                   |""".trimMargin()
            )
            if (enabled && job.isNotEmpty) {
                controller.setFontSize(controller.getPrinterInfo().printingFontType)
                job.commands.forEach { command ->
                    when (command) {
                        is PrintCommand.Barcode -> controller.printBarcode(command.barcode)
                        is PrintCommand.Image -> controller.printImage(command.image)
                        is PrintCommand.QrCode -> controller.printQr(command.code)
                        is PrintCommand.RawData -> controller.sendRawData(command.data)
                        is PrintCommand.Text -> controller.printText(command.text)
                        PrintCommand.CutPaper -> controller.cutPaper()
                        PrintCommand.FeedPaper -> controller.feedPaper()
                    }
                }
                analytics?.logPrintReceipt(job.description)
            }
        }.doOnError {
            logWarning("printing job '${job.description}'")
            analytics?.logErrorPrintReceipt("printing text '${job.description}'")
        }

    override suspend fun feedPaper(): PrinterResult<Unit> =
        printService.withPrinterCatching {
            if (enabled) {
                it.feedPaper()
            }
        }.doOnError {
            logWarning("feeding paper")
        }

    override suspend fun cutPaper(): PrinterResult<Unit> =
        printService.withPrinterCatching {
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