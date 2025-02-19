package de.tillhub.printengine.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import de.tillhub.printengine.PrintEngine
import de.tillhub.printengine.data.PrintCommand
import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.sample.ui.theme.TillhubPrintEngineTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val printer by lazy { PrintEngine.getInstance(this).printer }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val printState by printer.observePrinterState().collectAsState()
            TillhubPrintEngineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Layout(printState) {
                        lifecycleScope.launch {
                            printer.startPrintJob(printJob)
                        }
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    fun Layout(state: PrinterState, onClick: () -> Unit) {
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
            Button(onClick = onClick) {
                Text(text = "Print sample job")
            }
        }
    }

    companion object {
        private val printJob = PrintJob(listOf(
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
        ))
    }
}