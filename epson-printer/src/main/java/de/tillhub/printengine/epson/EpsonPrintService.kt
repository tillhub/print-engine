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
        EpsonPrinter(
            printer.info.deviceModel.uppercase().toModel(),
            EpsonPrinter.MODEL_ANK,
            context
        ).apply {
            setReceiveEventListener(receiveListener)

            try {
                connect(printer.connectionAddress, EpsonPrinter.PARAM_DEFAULT)
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

    @Suppress("CyclomaticComplexMethod")
    private fun String.toModel() = when (this) {
        "TM-M10" -> EpsonPrinter.TM_M10
        "TM-M30" -> EpsonPrinter.TM_M30
        "TM-P20" -> EpsonPrinter.TM_P20
        "TM-P60" -> EpsonPrinter.TM_P60
        "TM-P60II" -> EpsonPrinter.TM_P60II
        "TM-P80" -> EpsonPrinter.TM_P80
        "TM-T20" -> EpsonPrinter.TM_T20
        "TM-T60" -> EpsonPrinter.TM_T60
        "TM-T70" -> EpsonPrinter.TM_T70
        "TM-T81" -> EpsonPrinter.TM_T81
        "TM-T82" -> EpsonPrinter.TM_T82
        "TM-T83" -> EpsonPrinter.TM_T83
        "TM-T88" -> EpsonPrinter.TM_T88
        "TM-T90" -> EpsonPrinter.TM_T90
        "TM-T90KP" -> EpsonPrinter.TM_T90KP
        "TM-U220" -> EpsonPrinter.TM_U220
        "TM-U330" -> EpsonPrinter.TM_U330
        "TM-L90" -> EpsonPrinter.TM_L90
        "TM-H6000" -> EpsonPrinter.TM_H6000
        "TM-T83III" -> EpsonPrinter.TM_T83III
        "TM-T100" -> EpsonPrinter.TM_T100
        "TM-M30II" -> EpsonPrinter.TM_M30II
        "TS-100" -> EpsonPrinter.TS_100
        "TM-M50" -> EpsonPrinter.TM_M50
        "TM-T88VII" -> EpsonPrinter.TM_T88VII
        "TM-L90LFC" -> EpsonPrinter.TM_L90LFC
        "EU-M30" -> EpsonPrinter.EU_M30
        "TM-L100" -> EpsonPrinter.TM_L100
        "TM-P20II" -> EpsonPrinter.TM_P20II
        "TM-P80II" -> EpsonPrinter.TM_P80II
        "TM-M30III" -> EpsonPrinter.TM_M30III
        "TM-M50II" -> EpsonPrinter.TM_M50II
        "TM-M55" -> EpsonPrinter.TM_M55
        "TM-U220II" -> EpsonPrinter.TM_U220II
        "SB-H50" -> EpsonPrinter.SB_H50

        // based on the documentation these 2 can be valid
        "TM PRINTER",
        "" -> EpsonPrinter.TM_T88
        else -> throw IllegalArgumentException("Unsupported printer type: $this")
    }
}