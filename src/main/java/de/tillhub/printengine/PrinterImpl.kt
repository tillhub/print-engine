package de.tillhub.printengine

import de.tillhub.printengine.analytics.PrintAnalytics
import de.tillhub.printengine.data.PrintCommand
import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterConnectionState
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterResult
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.doOnError
import de.tillhub.printengine.pax.PaxPrintService
import de.tillhub.printengine.sunmi.SunmiPrintService
import de.tillhub.printengine.verifone.VerifonePrintService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class PrinterImpl(
    private val printService: PrintService,
    private val analytics: PrintAnalytics?
) : Printer {

    init {
        when (printService) {
            is PaxPrintService -> logInfo("PaxPrintService initialized")
            is SunmiPrintService -> logInfo("SunmiPrintService initialized")
            is VerifonePrintService -> logInfo("VerifonePrintService initialized")
        }
    }

    private var enabled: Boolean = true
    private var printingIntensity: PrintingIntensity = PrintingIntensity.DEFAULT

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun observeConnection(): StateFlow<PrinterConnectionState> = printService.printerConnectionState

    override fun observePrinterState(): StateFlow<PrinterState> =
        printService.withPrinterOrDefault(default = MutableStateFlow(PrinterState.Error.Unknown)) {
            it.observePrinterState()
        }

    override fun setPrintingIntensity(intensity: PrintingIntensity) {
        printingIntensity = intensity
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
                controller.setIntensity(printingIntensity)
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
                controller.start()
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
