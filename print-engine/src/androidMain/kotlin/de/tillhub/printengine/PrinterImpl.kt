package de.tillhub.printengine

import co.touchlab.kermit.Logger
import de.tillhub.printengine.analytics.PrintAnalytics
import de.tillhub.printengine.data.PrintCommand
import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterResult
import de.tillhub.printengine.data.PrinterSettings
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.doOnError
import de.tillhub.printengine.dispatcher.DispatcherProvider
import de.tillhub.printengine.dispatcher.DispatcherProviderImp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal class PrinterImpl(
    private val printService: PrintService,
    private val analytics: PrintAnalytics?,
    private val dispatcherProvider: DispatcherProvider = DispatcherProviderImp()
) : Printer {

    override val settings: PrinterSettings by lazy {
        PrinterSettings()
    }

    override val printerState: Flow<PrinterState> = printService.printerState

    override suspend fun getPrinterInfo(): PrinterResult<PrinterInfo> =
        withContext(dispatcherProvider.iO()) {
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
        }

    override suspend fun startPrintJob(job: PrintJob): PrinterResult<Unit> =
        withContext(dispatcherProvider.iO()) {
            printService.withPrinterCatching { controller ->
                logInfo(
                    """receipt: START #################
                   |${job.description}
                   |receipt END #################
                   |
                   """.trimMargin()
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
        }

    private fun logInfo(message: String) {
        Logger.i("printing: $message")
    }

    private fun logWarning(reason: String) {
        Logger.w("Printer not connected for: $reason")
    }
}
