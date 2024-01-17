package de.tillhub.printengine

import de.tillhub.printengine.analytics.PrintAnalytics
import de.tillhub.printengine.data.PrintCommand
import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterResult
import de.tillhub.printengine.data.PrinterSettings
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.doOnError
import de.tillhub.printengine.pax.PaxPrintService
import de.tillhub.printengine.sunmi.SunmiPrintService
import de.tillhub.printengine.verifone.VerifonePrintService
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

    override val settings: PrinterSettings by lazy {
        PrinterSettings()
    }

    override fun observePrinterState(): StateFlow<PrinterState> = printService.printerState

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
            if (settings.enabled && job.isNotEmpty) {
                controller.setIntensity(settings.printingIntensity)
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

    private fun logInfo(message: String) {
        Timber.i("printing: $message")
    }

    private fun logWarning(reason: String) {
        Timber.w("Printer not connected for: $reason")
    }
}
