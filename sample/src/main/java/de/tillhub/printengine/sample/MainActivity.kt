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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import de.tillhub.printengine.PrintEngine
import de.tillhub.printengine.data.PrintCommand
import de.tillhub.printengine.data.PrintJob
import de.tillhub.printengine.sample.ui.theme.TillhubPrintEngineTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val printer by lazy { PrintEngine.getInstance(this).printer }
    private val printerState = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                printer.observePrinterState().collect {
                    printerState.value = it.javaClass.simpleName
                }
            }
        }

        setContent {
            TillhubPrintEngineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Layout {
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
    fun Layout(onClick: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StateText()
            Spacer(modifier = Modifier.height(36.dp))
            Button(onClick = onClick) {
                Text(text = "Print sample job")
            }
        }
    }

    @Preview
    @Composable
    fun StateText() {
        val value by printerState
        Text(
            text = "Printer state: $value",
            textAlign = TextAlign.Center
        )
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
            PrintCommand.FeedPaper,
        ))
    }
}