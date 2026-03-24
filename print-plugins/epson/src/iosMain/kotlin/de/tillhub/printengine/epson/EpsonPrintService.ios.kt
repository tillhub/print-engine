package de.tillhub.printengine.epson

import co.touchlab.kermit.Logger
import com.epson.epos2.EPOS2_MODEL_ANK
import com.epson.epos2.EPOS2_TM_H6000
import com.epson.epos2.EPOS2_TM_L100
import com.epson.epos2.EPOS2_TM_L90
import com.epson.epos2.EPOS2_TM_L90LFC
import com.epson.epos2.EPOS2_TM_M10
import com.epson.epos2.EPOS2_TM_M30
import com.epson.epos2.EPOS2_TM_M30II
import com.epson.epos2.EPOS2_TM_M30III
import com.epson.epos2.EPOS2_TM_M50
import com.epson.epos2.EPOS2_TM_P20
import com.epson.epos2.EPOS2_TM_P20II
import com.epson.epos2.EPOS2_TM_P60
import com.epson.epos2.EPOS2_TM_P60II
import com.epson.epos2.EPOS2_TM_P80
import com.epson.epos2.EPOS2_TM_P80II
import com.epson.epos2.EPOS2_TM_T100
import com.epson.epos2.EPOS2_TM_T20
import com.epson.epos2.EPOS2_TM_T60
import com.epson.epos2.EPOS2_TM_T70
import com.epson.epos2.EPOS2_TM_T81
import com.epson.epos2.EPOS2_TM_T82
import com.epson.epos2.EPOS2_TM_T83
import com.epson.epos2.EPOS2_TM_T83III
import com.epson.epos2.EPOS2_TM_T88
import com.epson.epos2.EPOS2_TM_T88VII
import com.epson.epos2.EPOS2_TM_T90
import com.epson.epos2.EPOS2_TM_T90KP
import com.epson.epos2.EPOS2_TM_U220
import com.epson.epos2.EPOS2_TM_U330
import com.epson.epos2.EPOS2_TS_100
import com.epson.epos2.Epos2Printer
import com.epson.epos2.Epos2PrinterStatusInfo
import com.epson.epos2.Epos2PtrReceiveDelegateProtocol
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.darwin.NSObject

private val logger = Logger.withTag("EpsonPrintService")

@OptIn(ExperimentalForeignApi::class)
internal actual class EpsonPrintService(
    printer: ExternalPrinter,
) : PrintService() {

    private val connectionState = MutableStateFlow<PrinterState>(PrinterState.CheckingForPrinter)
    actual override val printerState: Flow<PrinterState> = connectionState

    private val receiveDelegate = object : NSObject(), Epos2PtrReceiveDelegateProtocol {
        override fun onPtrReceive(
            printerObj: Epos2Printer?,
            code: Int,
            status: Epos2PrinterStatusInfo?,
            printJobId: String?,
        ) {
            connectionState.value = EpsonPrinterErrorState.epsonStatusToState(code, status)
        }
    }

    private val epsonPrinter: Epos2Printer by lazy {
        connectionState.value = PrinterState.Preparing
        val series = printer.info.deviceModel.uppercase().toModel()
        logger.d { "Initializing Epos2Printer series=$series model=${printer.info.deviceModel}" }
        Epos2Printer(printerSeries = series, lang = EPOS2_MODEL_ANK).apply {
            setReceiveEventDelegate(receiveDelegate)
            connectionState.value = PrinterState.Connected
        }
    }

    actual override var printController: PrinterController? =
        EpsonPrinterController(
            printerData = printer,
            epsonPrinter = epsonPrinter,
            printerState = connectionState,
        )

    @Suppress("CyclomaticComplexMethod")
    private fun String.toModel(): Int = when (this.substringBefore('_')) {
        "TM-M10" -> EPOS2_TM_M10
        "TM-M30" -> EPOS2_TM_M30
        "TM-P20" -> EPOS2_TM_P20
        "TM-P60" -> EPOS2_TM_P60
        "TM-P60II" -> EPOS2_TM_P60II
        "TM-P80" -> EPOS2_TM_P80
        "TM-T20" -> EPOS2_TM_T20
        "TM-T60" -> EPOS2_TM_T60
        "TM-T70" -> EPOS2_TM_T70
        "TM-T81" -> EPOS2_TM_T81
        "TM-T82" -> EPOS2_TM_T82
        "TM-T83" -> EPOS2_TM_T83
        "TM-T88" -> EPOS2_TM_T88
        "TM-T90" -> EPOS2_TM_T90
        "TM-T90KP" -> EPOS2_TM_T90KP
        "TM-U220" -> EPOS2_TM_U220
        "TM-U330" -> EPOS2_TM_U330
        "TM-L90" -> EPOS2_TM_L90
        "TM-H6000" -> EPOS2_TM_H6000
        "TM-T83III" -> EPOS2_TM_T83III
        "TM-T100" -> EPOS2_TM_T100
        "TM-M30II" -> EPOS2_TM_M30II
        "TS-100" -> EPOS2_TS_100
        "TM-M50" -> EPOS2_TM_M50
        "TM-T88VII" -> EPOS2_TM_T88VII
        "TM-L90LFC" -> EPOS2_TM_L90LFC
        "TM-L100" -> EPOS2_TM_L100
        "TM-P20II" -> EPOS2_TM_P20II
        "TM-P80II" -> EPOS2_TM_P80II
        "TM-M30III" -> EPOS2_TM_M30III
        // Models not in SDK 2.23.1 — fall back to closest equivalent
        "TM-M50II", "TM-M55" -> EPOS2_TM_M50
        "TM-U220II" -> EPOS2_TM_U220
        "EU-M30" -> EPOS2_TM_M30
        "SB-H50" -> EPOS2_TM_T88
        "TM PRINTER", "" -> EPOS2_TM_T88
        else -> {
            logger.w { "Unknown printer model '$this', falling back to TM-T88" }
            EPOS2_TM_T88
        }
    }
}
