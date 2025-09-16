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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import de.tillhub.printengine.PrintEngine
import de.tillhub.printengine.data.DiscoveryState
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrintCommand
import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.epson.EpsonPrinterDiscovery
import de.tillhub.printengine.sample.ui.PrinterLayout
import de.tillhub.printengine.sample.ui.theme.TillhubPrintEngineTheme
import de.tillhub.printengine.star.StarPrinterDiscovery
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var initilazed: Boolean = false
    private val printerEngine by lazy {
        PrintEngine.getInstance().also {
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
                            if (!initilazed) {
                                initilazed = true
                                lifecycleScope.launch {
                                    printerEngine.initPrinter { barcodeEncoder ->
                                        PrinterServiceFactory.createPrinterService(
                                            context = this@MainActivity,
                                            externalPrinter = printer,
                                            barcode = barcodeEncoder
                                        )
                                    }
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



    private fun discoverPrinters() {
        lifecycleScope.launch {
            printerEngine.discoverExternalPrinters(
                StarPrinterDiscovery(this@MainActivity),
                EpsonPrinterDiscovery(this@MainActivity)
            ).collect { discoveryState ->
                when (discoveryState) {
                    is DiscoveryState.Discovering -> {
                        printers.apply {
                            clear()
                            addAll(discoveryState.printers)
                        }
                    }

                    is DiscoveryState.Finished -> {
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
                            "              receipt-footer              \n"
                ),
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
                PrintCommand.FeedPaper,
                PrintCommand.CutPaper
            )
        )
    }
}