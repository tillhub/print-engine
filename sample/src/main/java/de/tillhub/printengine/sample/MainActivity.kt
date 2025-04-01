package de.tillhub.printengine.sample

import StarPrintService
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import de.tillhub.printengine.PrintEngine
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrintCommand
import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.sample.ui.theme.TillhubPrintEngineTheme
import de.tillhub.printengine.star.StarManufacturer
import de.tillhub.printengine.star.StarPrinterDiscovery
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val printerEngine by lazy { PrintEngine.getInstance(this) }
    private val printers = mutableStateListOf<ExternalPrinter>()

    private val requestCode = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val printState by printerEngine.printer.observePrinterState().collectAsState()
            TillhubPrintEngineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Layout(printState, printers) { printer ->
                        lifecycleScope.launch {
                            when (printer?.manufacturer) {
                                StarManufacturer -> printerEngine.initPrinter(
                                    StarPrintService(this@MainActivity, printer)
                                )

                                else -> Unit
                            }
                            printerEngine.printer.startPrintJob(printJob)
                        }
                    }
                }
            }
        }
        requestBluetoothPermission()
        lifecycleScope.launch {
            printerEngine.discoverExternalPrinters(StarPrinterDiscovery)
                .collect { discoveryState ->
                    when (discoveryState) {
                        is DiscoveryState.Discovering,  -> {
                            printers.clear()
                            printers.addAll(discoveryState.printers)
                        }
                        is DiscoveryState.Discovered -> {
                            printers.clear()
                            printers.addAll(discoveryState.printers)
                        }
                        else -> Unit
                    }
                }
        }
    }

    @Preview
    @Composable
    fun Layout(
        state: PrinterState,
        printers: List<ExternalPrinter>,
        onClick: (ExternalPrinter?) -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Printer state: $state",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(36.dp))
            if (printers.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(printers) { printer ->
                        PrinterItem(printerInfo = printer, onClick = { onClick(printer) })
                    }
                }
            } else {
                Button(onClick = { onClick(null) }) {
                    Text(text = "Print sample job")
                }
            }
        }


    }

    @Composable
    fun PrinterItem(printerInfo: ExternalPrinter, onClick: (ExternalPrinter) -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { onClick(printerInfo) },
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = printerInfo.connectionAddress,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = printerInfo.info.deviceModel,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return
        }

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                ), requestCode
            )
        }
    }

    private fun hasBluetoothPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        return checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val printJob = PrintJob(
            listOf(
                PrintCommand.Text("This is a line"),
                PrintCommand.Text("This is a another line"),
                PrintCommand.Text("-------"),
                PrintCommand.Text("Barcode:"),
                PrintCommand.Barcode("123ABC"),
                PrintCommand.Text("QR code:"),
                PrintCommand.QrCode("123ABC"),
                PrintCommand.Text("40 char line:"),
                PrintCommand.Text("1234567890123456789012345678901234567890"),
                PrintCommand.FeedPaper,
            )
        )

        private val printJobw = PrintJob(
            listOf(
                PrintCommand.Text("This is a line erf klenvkerv e nckler\n"),
                PrintCommand.Text("This is a another line wef kwef;e\n"),
                PrintCommand.Text("------33333-\n"),
                PrintCommand.Text("Barcodeevr:"),
                PrintCommand.Barcode("123ABevvC"),
                PrintCommand.Text("QR cerverode:"),
                PrintCommand.QrCode("123evefvABC"),
                PrintCommand.Text("40 chaevrevr line:\n"),
                PrintCommand.Text("1234567890123456789033cccc12345678901234567890"),
                PrintCommand.FeedPaper,
            )
        )
    }
}