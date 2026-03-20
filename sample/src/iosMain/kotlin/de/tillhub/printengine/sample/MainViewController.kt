package de.tillhub.printengine.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrintCommand
import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.epson.EpsonServiceProvider
import de.tillhub.printengine.sample.ui.PrinterLayout
import de.tillhub.printengine.sample.ui.theme.TillhubPrintEngineTheme
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
    val scope = rememberCoroutineScope()

    val printState by printEngine.printer.printerState
        .collectAsState(PrinterState.CheckingForPrinter)

    TillhubPrintEngineTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            PrinterLayout(
                printState = printState,
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
