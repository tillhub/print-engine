package de.tillhub.printengine.external

import de.tillhub.printengine.Printer
import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterResult
import de.tillhub.printengine.data.PrinterSettings
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.emulated.EmulatedPrinter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

internal class ExternalPrinterContainer(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : Printer {
    private val selectedPrinter = MutableStateFlow<Printer>(EmulatedPrinter())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val printerState: StateFlow<PrinterState> = selectedPrinter
        .flatMapLatest { printer -> printer.observePrinterState() }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = PrinterState.CheckingForPrinter
        )

    override val settings: PrinterSettings get() = selectedPrinter.value.settings
    override fun observePrinterState(): StateFlow<PrinterState> = printerState
    override suspend fun getPrinterInfo(): PrinterResult<PrinterInfo> =
        selectedPrinter.value.getPrinterInfo()

    override suspend fun startPrintJob(job: PrintJob): PrinterResult<Unit> =
        selectedPrinter.value.startPrintJob(job)

    fun initPrinter(printer: Printer) {
        selectedPrinter.value = printer
    }
}