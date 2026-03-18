package de.tillhub.printengine.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import de.tillhub.printengine.PrintEngine
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.sample.ui.PrinterLayout
import de.tillhub.printengine.sample.ui.theme.TillhubPrintEngineTheme
import platform.UIKit.UIViewController

@Suppress("ktlint:standard:function-naming")
fun MainViewController(): UIViewController = ComposeUIViewController {
    remember {
        PrintEngine.getInstance().also {
            it.printer.settings.printingIntensity = PrintingIntensity.DEFAULT
        }
    }
    val printers = remember { mutableStateListOf<ExternalPrinter>() }

    TillhubPrintEngineTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            PrinterLayout(
                printState = PrinterState.CheckingForPrinter,
                printers = printers,
                onPrinterSelected = { _ ->
                    // Printing not available on iOS simulator without a physical printer
                },
            )
        }
    }
}
