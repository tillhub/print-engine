package de.tillhub.printengine.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import de.tillhub.printengine.PrintEngine
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrintCommand
import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.epson.EpsonPrinterDiscovery
import de.tillhub.printengine.epson.EpsonServiceProvider
import de.tillhub.printengine.sample.ui.PrinterLayout
import de.tillhub.printengine.sample.ui.theme.TillhubPrintEngineTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import platform.UIKit.UIViewController

@Suppress("ktlint:standard:function-naming")
fun MainViewController(): UIViewController = ComposeUIViewController {
    val printEngine = remember {
        PrintEngine.getInstance().also {
            it.printer.settings.printingIntensity = PrintingIntensity.DEFAULT
        }
    }

    val printers = remember { mutableStateListOf<ExternalPrinter>() }
    var initialized by remember { mutableStateOf(false) }
    var discoveryStatus by remember { mutableStateOf("Searching for printers…") }
    val scope = rememberCoroutineScope()

    // Collect discovery flow on IO, but update Compose state on Main via flowOn
    LaunchedEffect(Unit) {
        printEngine.discoverExternalPrinters(
            EpsonPrinterDiscovery(),
        ).flowOn(Dispatchers.IO).collect { state ->
            when (state) {
                is DiscoveryState.Idle -> discoveryStatus = "Searching for printers…"
                is DiscoveryState.Discovering -> {
                    printers.clear()
                    printers.addAll(state.printers)
                    discoveryStatus = "Found ${state.printers.size} printer(s)"
                }
                is DiscoveryState.Finished -> {
                    printers.clear()
                    printers.addAll(state.printers)
                    discoveryStatus = "Discovery done: ${state.printers.size} printer(s)"
                }
                is DiscoveryState.Error -> {
                    discoveryStatus = "Discovery error: ${state.message}"
                }
            }
        }
    }

    val printState by printEngine.printer.printerState
        .collectAsState(PrinterState.CheckingForPrinter)

    TillhubPrintEngineTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            PrinterLayout(
                printState = printState,
                discoveryStatus = discoveryStatus,
                printers = printers,
                onPrinterSelected = { selectedPrinter ->
                    scope.launch {
                        if (!initialized && selectedPrinter != null) {
                            try {
                                printEngine.initPrinter {
                                    EpsonServiceProvider.build(printer = selectedPrinter)
                                }
                                initialized = true
                            } catch (_: Exception) {
                                discoveryStatus = "Failed to initialize printer"
                                return@launch
                            }
                        }
                        if (initialized) {
                            printEngine.printer.startPrintJob(PRINT_JOB)
                        }
                    }
                },
            )
        }
    }
}

private val PRINT_JOB = PrintJob(
    listOf(
        PrintCommand.Text(
            "              receipt-header              \n" +
                "------------------------------------------\n" +
                "Receipt:                                 1\n" +
                "Salesperson:                    staff name\n" +
                "Date and Time:             20.5.2020 20:00\n" +
                "Branch:                                  1\n" +
                "Register:                              123\n" +
                "------------------------------------------\n" +
                "Customer number:                    654321\n" +
                "------------------------------------------\n" +
                "  receipt-note                            \n" +
                "------------------------------------------\n" +
                "0011                                      \n" +
                "multiline long product item name for      \n" +
                "receipt att-desc                          \n" +
                "- addon_name                              \n" +
                "             10.00 €       1       10.00 €\n" +
                "  note                                    \n" +
                "voucher-code                              \n" +
                "                                          \n" +
                "------------------------------------------\n" +
                "Tips                                3.00 €\n" +
                "                                          \n" +
                "******************************************\n" +
                "Total (gross):                     10.00 €\n" +
                "******************************************\n" +
                "Given:                                    \n" +
                "Cash                               10.00 €\n" +
                "- including tip:                    3.00 €\n" +
                "------------------------------------------\n" +
                "Change:                            10.00 €\n" +
                "------------------------------------------\n" +
                "Vat                                       \n" +
                "    19 %                            1.00 €\n" +
                "------------------------------------------\n" +
                "Net:                               10.00 €\n" +
                "------------------------------------------\n" +
                "              receipt-footer              \n",
        ),
        PrintCommand.Text("This is another line"),
        PrintCommand.Text("-------"),
        PrintCommand.Text("Barcode working:"),
        PrintCommand.Barcode("RTC6093739"),
        PrintCommand.Text("QR code:"),
        PrintCommand.QrCode("123ABC"),
        PrintCommand.Text("40 char line:"),
        PrintCommand.Text("1234567890123456789012345678901234567890"),
        PrintCommand.FeedPaper,
        PrintCommand.CutPaper,
    ),
)
