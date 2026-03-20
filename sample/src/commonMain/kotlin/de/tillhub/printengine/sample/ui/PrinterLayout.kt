package de.tillhub.printengine.sample.ui

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun PrinterLayout(
    printState: PrinterState,
    printers: List<ExternalPrinter>,
    onPrinterSelected: (ExternalPrinter?) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Printer state: $printState",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(36.dp))
        Button(onClick = { onPrinterSelected(null) }) {
            Text(text = "Print sample job")
        }
        Spacer(modifier = Modifier.height(36.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(printers, key = { it.connectionAddress }) { printer ->
                PrinterItem(
                    printerInfo = printer,
                    onClick = { onPrinterSelected(printer) },
                )
            }
        }
    }
}

@Composable
private fun PrinterItem(
    printerInfo: ExternalPrinter,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = printerInfo.connectionAddress,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = printerInfo.info.deviceModel,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
