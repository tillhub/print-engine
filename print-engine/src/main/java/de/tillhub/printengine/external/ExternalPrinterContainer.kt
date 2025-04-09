package de.tillhub.printengine.external

import de.tillhub.printengine.Printer
import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterResult
import de.tillhub.printengine.data.PrinterSettings
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.emulated.EmulatedPrinter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

internal class ExternalPrinterContainer : Printer {
    private val selectedPrinter = MutableStateFlow<Printer>(EmulatedPrinter())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val printerState: Flow<PrinterState> = selectedPrinter
        .flatMapLatest { it.printerState }

    override val settings: PrinterSettings get() = selectedPrinter.value.settings

    override suspend fun getPrinterInfo(): PrinterResult<PrinterInfo> =
        selectedPrinter.value.getPrinterInfo()

    override suspend fun startPrintJob(job: PrintJob): PrinterResult<Unit> =
        selectedPrinter.value.startPrintJob(job)

    fun initPrinter(printer: Printer) {
        selectedPrinter.value = printer
    }
}