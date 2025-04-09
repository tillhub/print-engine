package de.tillhub.printengine.epson

import android.content.Context
import com.epson.epos2.Epos2CallbackCode
import com.epson.epos2.printer.ReceiveListener
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.ConnectionType
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.epson.epos2.printer.Printer as EpsonPrinter

class EpsonPrintService(context: Context, printer: ExternalPrinter) : PrintService() {

    private val connectionState = MutableStateFlow<PrinterState>(PrinterState.CheckingForPrinter)
    override val printerState: StateFlow<PrinterState> = connectionState

    private val receiveListener = ReceiveListener { _, code, status, _ ->
        connectionState.value = when (code) {
            Epos2CallbackCode.CODE_SUCCESS -> PrinterState.Connected
            Epos2CallbackCode.CODE_PRINTING -> PrinterState.Busy
            else -> when {
                status.online == EpsonPrinter.FALSE -> PrinterState.Error.ConnectionLost
                status.paper == EpsonPrinter.PAPER_EMPTY -> PrinterState.Error.OutOfPaper
                status.coverOpen == EpsonPrinter.TRUE -> PrinterState.Error.CoverNotClosed
                status.errorStatus == EpsonPrinter.UNKNOWN ||
                        status.errorStatus == EpsonPrinter.MECHANICAL_ERR -> {
                    PrinterState.Error.Malfunctions
                }
                status.errorStatus == EpsonPrinter.AUTOCUTTER_ERR -> {
                    PrinterState.Error.PaperCutterAbnormal
                }
                status.errorStatus == EpsonPrinter.UNRECOVER_ERR -> {
                    when (status.unrecoverError) {
                        EpsonPrinter.LOW_VOLTAGE_ERR -> PrinterState.Error.VoltageTooLow
                        else -> PrinterState.Error.Malfunctions
                    }
                }
                status.errorStatus == EpsonPrinter.AUTORECOVER_ERR -> {
                    when (status.autoRecoverError) {
                        EpsonPrinter.HEAD_OVERHEAT,
                        EpsonPrinter.MOTOR_OVERHEAT,
                        EpsonPrinter.BATTERY_OVERHEAT -> PrinterState.Error.Overheated
                        EpsonPrinter.WRONG_PAPER -> PrinterState.Error.PaperAbnormal
                        EpsonPrinter.COVER_OPEN -> PrinterState.Error.CoverNotClosed
                        else -> PrinterState.Error.Malfunctions
                    }
                }
                status.batteryLevel == EpsonPrinter.BATTERY_LEVEL_0 -> {
                    PrinterState.Error.VoltageTooLow
                }

                else -> PrinterState.Connected
            }
        }
    }

    private val epsonPrinter: EpsonPrinter by lazy {
        EpsonPrinter(printer.info.deviceModel.toModel(), EpsonPrinter.MODEL_ANK, context).apply {
            setReceiveEventListener(receiveListener)

            val connectionUri = "${printer.connectionType.toProtocol()}:${printer.connectionAddress}"
            try {
                connect(connectionUri, EpsonPrinter.PARAM_DEFAULT)
                connectionState.value = PrinterState.Connected
            } catch (_: Exception) {
                connectionState.value = PrinterState.Error.AbnormalCommunication
            }
        }
    }

    override var printController: PrinterController? = EpsonPrinterController(
        printerData = printer,
        epsonPrinter = epsonPrinter,
        printerState = connectionState
    )

    private fun ConnectionType.toProtocol() = when (this) {
        ConnectionType.LAN -> "TCP"
        ConnectionType.BLUETOOTH -> "BT"
        ConnectionType.USB -> "USB"
    }

    @Suppress("CyclomaticComplexMethod")
    private fun String.toModel() = when (this) {
        "TM_M10" -> EpsonPrinter.TM_M10
        "TM_M30" -> EpsonPrinter.TM_M30
        "TM_P20" -> EpsonPrinter.TM_P20
        "TM_P60" -> EpsonPrinter.TM_P60
        "TM_P60II" -> EpsonPrinter.TM_P60II
        "TM_P80" -> EpsonPrinter.TM_P80
        "TM_T20" -> EpsonPrinter.TM_T20
        "TM_T60" -> EpsonPrinter.TM_T60
        "TM_T70" -> EpsonPrinter.TM_T70
        "TM_T81" -> EpsonPrinter.TM_T81
        "TM_T82" -> EpsonPrinter.TM_T82
        "TM_T83" -> EpsonPrinter.TM_T83
        "TM_T88" -> EpsonPrinter.TM_T88
        "TM_T90" -> EpsonPrinter.TM_T90
        "TM_T90KP" -> EpsonPrinter.TM_T90KP
        "TM_U220" -> EpsonPrinter.TM_U220
        "TM_U330" -> EpsonPrinter.TM_U330
        "TM_L90" -> EpsonPrinter.TM_L90
        "TM_H6000" -> EpsonPrinter.TM_H6000
        "TM_T83III" -> EpsonPrinter.TM_T83III
        "TM_T100" -> EpsonPrinter.TM_T100
        "TM_M30II" -> EpsonPrinter.TM_M30II
        "TS_100" -> EpsonPrinter.TS_100
        "TM_M50" -> EpsonPrinter.TM_M50
        "TM_T88VII" -> EpsonPrinter.TM_T88VII
        "TM_L90LFC" -> EpsonPrinter.TM_L90LFC
        "EU_M30" -> EpsonPrinter.EU_M30
        "TM_L100" -> EpsonPrinter.TM_L100
        "TM_P20II" -> EpsonPrinter.TM_P20II
        "TM_P80II" -> EpsonPrinter.TM_P80II
        "TM_M30III" -> EpsonPrinter.TM_M30III
        "TM_M50II" -> EpsonPrinter.TM_M50II
        "TM_M55" -> EpsonPrinter.TM_M55
        "TM_U220II" -> EpsonPrinter.TM_U220II
        "SB_H50" -> EpsonPrinter.SB_H50

        // based on the documentation these 2 can be valid
        "TM Printer" -> EpsonPrinter.TM_T88
        "" -> EpsonPrinter.TM_T88
        else -> throw IllegalArgumentException("Unsupported printer type: $this")
    }
}