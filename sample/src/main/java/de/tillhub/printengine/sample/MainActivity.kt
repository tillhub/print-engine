package de.tillhub.printengine.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.CardDefaults
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
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.epson.EpsonPrinterDiscovery
import de.tillhub.printengine.epson.EpsonServiceProvider
import de.tillhub.printengine.sample.ui.theme.TillhubPrintEngineTheme
import de.tillhub.printengine.star.StarPrinterDiscovery
import de.tillhub.printengine.star.StarServiceProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var initilazed: Boolean = false
    private val printerEngine by lazy {
        PrintEngine.getInstance(this).also {
            it.printer.settings.printingIntensity = PrintingIntensity.DEFAULT
        }
    }
    private val printers = mutableStateListOf<ExternalPrinter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestBluetoothPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                discoverPrinters()
            } else {
                showToast("Bluetooth permission is required to connect to printers")
            }
        }

        setContent {
            TillhubPrintEngineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val printState by printerEngine.printer.printerState
                        .collectAsState(PrinterState.CheckingForPrinter)
                    PrinterLayout(
                        printState = printState,
                        printers = printers,
                        onPrinterSelected = { printer ->
                            if (printer != null && !initilazed) {
                                initilazed = true
                                lifecycleScope.launch {
                                    val service = when (printer.manufacturer) {
                                        "EPSON" -> EpsonServiceProvider.build(
                                            context = this@MainActivity,
                                            printer
                                        )

                                        else -> StarServiceProvider.build(
                                            context = this@MainActivity,
                                            printer
                                        )
                                    }
                                    printerEngine.initPrinter(service)
                                }
                            }
                            lifecycleScope.launch {
                                printerEngine.printer.startPrintJob(printJob)
                            }
                        }
                    )
                }
            }
        }

        requestBluetoothPermission(requestBluetoothPermissionLauncher)
    }

    @Preview
    @Composable
    private fun PrinterLayout(
        printState: PrinterState,
        printers: List<ExternalPrinter>,
        onPrinterSelected: (ExternalPrinter?) -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Printer state: $printState",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(36.dp))
            if (printers.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(printers, key = { it.connectionAddress }) { printer ->
                        PrinterItem(
                            printerInfo = printer,
                            onClick = { onPrinterSelected(printer) }
                        )
                    }
                }
            } else {
                Button(onClick = { onPrinterSelected(null) }) {
                    Text(text = "Print sample job")
                }
            }
        }
    }

    @Composable
    private fun PrinterItem(
        printerInfo: ExternalPrinter,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable(onClick = onClick),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

    private fun discoverPrinters() {
        lifecycleScope.launch {
            printerEngine.discoverExternalPrinters(
                StarPrinterDiscovery,
                EpsonPrinterDiscovery
            ).collect { discoveryState ->
                when (discoveryState) {
                    is DiscoveryState.Discovering -> {
                        printers.apply {
                            clear()
                            addAll(discoveryState.printers)
                        }
                    }

                    is DiscoveryState.Discovered -> {
                        printers.apply {
                            clear()
                            addAll(discoveryState.printers)
                        }
                    }

                    is DiscoveryState.Error -> {
                        showToast("Discovery error: ${discoveryState.message}")
                    }

                    DiscoveryState.Idle -> Unit
                }
            }
        }
    }

    private fun requestBluetoothPermission(launcher: ActivityResultLauncher<Array<String>>) {
        val permissions = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            mutableListOf<String>().apply {
                if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    add(Manifest.permission.BLUETOOTH)
                }

                if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    add(Manifest.permission.BLUETOOTH_ADMIN)
                }

                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            mutableListOf<String>().apply {
                if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    add(Manifest.permission.BLUETOOTH)
                }

                if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    add(Manifest.permission.BLUETOOTH_ADMIN)
                }

                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        } else {
            mutableListOf<String>().apply {
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    add(Manifest.permission.BLUETOOTH_SCAN)
                }

                if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    add(Manifest.permission.BLUETOOTH_CONNECT)
                }
            }
        }

        if (permissions.isNotEmpty()) {
            launcher.launch(permissions.toTypedArray())
        } else {
            discoverPrinters()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private val printJob = PrintJob(
            listOf(
                PrintCommand.Text("This is a line"),
                PrintCommand.Text("This is a another line"),
                PrintCommand.Text("-------"),
                PrintCommand.Text("Barcode working:"),
                PrintCommand.Barcode("RTC6093739"),
                PrintCommand.Text("Barcode broken:"),
                PrintCommand.Barcode("RTB183648B"),
                PrintCommand.Text("Barcode more broken:"),
                PrintCommand.Barcode("RTABCDEFAB"),
                PrintCommand.Text("QR code:"),
                PrintCommand.QrCode("123ABC"),
                PrintCommand.Text("40 char line:"),
                PrintCommand.Text("1234567890123456789012345678901234567890"),
                PrintCommand.FeedPaper
            )
        )
    }
}