package de.tillhub.printengine.emulated

import de.tillhub.printengine.Printer
import de.tillhub.printengine.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

/**
 * EmulatedPrinter is used when Printer is not available or supported by device
 */
class EmulatedPrinter : Printer {

    init {
        logInfo("EmulatedPrintService initialized")
    }

    override fun setEnabled(enabled: Boolean) {
        if (enabled) logInfo("printer enabled")
        else logInfo("printer disabled")
    }

    override fun observeConnection(): StateFlow<PrinterConnectionState> {
        return MutableStateFlow(PrinterConnectionState.PrinterNotAvailable)
    }

    override fun observePrinterState(): StateFlow<PrinterState> {
        return MutableStateFlow(PrinterState.PrinterNotDetected)
    }

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

    override suspend fun startPrintJob(job: PrintJob): PrinterResult<Unit> {
        logInfo(
            """receipt: START #################
                   |${job.description}
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
