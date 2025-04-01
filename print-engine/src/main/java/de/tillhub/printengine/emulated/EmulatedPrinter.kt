package de.tillhub.printengine.emulated

import de.tillhub.printengine.Printer
import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterResult
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterSettings
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingPaperSpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

/**
 * EmulatedPrinter is used when Printer is not available or supported by device
 */
internal class EmulatedPrinter : Printer {

    init {
        logInfo("EmulatedPrintService initialized")
    }

    override val settings: PrinterSettings by lazy {
        PrinterSettings()
    }

    override fun observePrinterState(): StateFlow<PrinterState> {
        return MutableStateFlow(PrinterState.Error.NotAvailable)
    }

    override suspend fun getPrinterInfo(): PrinterResult<PrinterInfo> {
        return PrinterResult.Success(
            PrinterInfo(
                serialNumber = "n/a",
                deviceModel = "Emulated Printer",
                printerVersion = "n/a",
                printerPaperSpec = PrintingPaperSpec.PaxPaper56mm,
                printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
                printerHead = "n/a",
                printedDistance = 0,
                serviceVersion = PrinterServiceVersion.Info(
                    serviceVersionName = "1.0.0",
                    serviceVersionCode = 1
                )
            )
        )
    }

    override suspend fun startPrintJob(job: PrintJob): PrinterResult<Unit> {
        logInfo(
            """receipt: START #################
                   |${job.description}
                   |receipt END #################
                   |
                   """.trimMargin()
        )
        return PrinterResult.Success(Unit)
    }

    private fun logInfo(message: String) {
        Timber.i("EmulatedPrinter: $message")
    }
}
