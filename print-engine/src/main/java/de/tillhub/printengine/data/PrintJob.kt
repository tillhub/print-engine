package de.tillhub.printengine.data

import java.util.Objects

class PrintJob(val commands: List<PrintCommand>) {
    val isNotEmpty = commands.isNotEmpty()
    val description: String by lazy {
        commands.joinToString(separator = "\n") { command ->
            when (command) {
                is PrintCommand.Barcode -> "==BC: ${command.barcode} =="
                PrintCommand.CutPaper -> "------CUT PAPER-----"
                PrintCommand.FeedPaper -> "-----FEED PAPER-----"
                is PrintCommand.Image -> "======IMAGE========"
                is PrintCommand.QrCode -> "==QR: ${command.code} =="
                is PrintCommand.RawData -> command.data.bytes.toString(Charsets.UTF_8)
                is PrintCommand.Text -> command.text
            }
        }
    }

    override fun toString() = "PrintJob(" +
            "commands=$commands" +
            ")"

    override fun equals(other: Any?) = other is PrintJob &&
            commands == other.commands

    override fun hashCode() = Objects.hash(commands)
}
