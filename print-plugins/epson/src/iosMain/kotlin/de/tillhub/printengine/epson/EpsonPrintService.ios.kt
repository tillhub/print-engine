package de.tillhub.printengine.epson

import co.touchlab.kermit.Logger
import com.epson.epos2.Epos2PrinterStatusInfo
import com.epson.epos2.Epos2PtrReceiveDelegateProtocol
import com.epson.epos2.Epos2Printer
import kotlinx.cinterop.ExperimentalForeignApi
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterState
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
        "TM-M10"   -> EPOS2_TM_M10
        "TM-M30"   -> EPOS2_TM_M30
        "TM-P20"   -> EPOS2_TM_P20
        "TM-P60"   -> EPOS2_TM_P60
        "TM-P60II" -> EPOS2_TM_P60II
        "TM-P80"   -> EPOS2_TM_P80
        "TM-T20"   -> EPOS2_TM_T20
        "TM-T60"   -> EPOS2_TM_T60
        "TM-T70"   -> EPOS2_TM_T70
        "TM-T81"   -> EPOS2_TM_T81
        "TM-T82"   -> EPOS2_TM_T82
        "TM-T83"   -> EPOS2_TM_T83
        "TM-T88"   -> EPOS2_TM_T88
        "TM-T90"   -> EPOS2_TM_T90
        "TM-T90KP" -> EPOS2_TM_T90KP
        "TM-U220"  -> EPOS2_TM_U220
        "TM-U330"  -> EPOS2_TM_U330
        "TM-L90"   -> EPOS2_TM_L90
        "TM-H6000" -> EPOS2_TM_H6000
        "TM-T83III"-> EPOS2_TM_T83III
        "TM-T100"  -> EPOS2_TM_T100
        "TM-M30II" -> EPOS2_TM_M30II
        "TS-100"   -> EPOS2_TS_100
        "TM-M50"   -> EPOS2_TM_M50
        "TM-T88VII"-> EPOS2_TM_T88VII
        "TM-L90LFC"-> EPOS2_TM_L90LFC
        "TM-L100"  -> EPOS2_TM_L100
        "TM-P20II" -> EPOS2_TM_P20II
        "TM-P80II" -> EPOS2_TM_P80II
        "TM-M30III"-> EPOS2_TM_M30III
        // Models not in SDK 2.23.1 — fall back to closest equivalent
        "TM-M50II", "TM-M55" -> EPOS2_TM_M50
        "TM-U220II"           -> EPOS2_TM_U220
        "EU-M30"              -> EPOS2_TM_M30
        "SB-H50"              -> EPOS2_TM_T88
        "TM PRINTER", ""      -> EPOS2_TM_T88
        else -> {
            logger.w { "Unknown printer model '$this', falling back to TM-T88" }
            EPOS2_TM_T88
        }
    }

    // SDK 2.23.1 series constants (values match the NS_ENUM order in ePOS2.h)
    private companion object {
        const val EPOS2_TM_M10    = 0
        const val EPOS2_TM_M30    = 1
        const val EPOS2_TM_P20    = 2
        const val EPOS2_TM_P60    = 3
        const val EPOS2_TM_P60II  = 4
        const val EPOS2_TM_P80    = 5
        const val EPOS2_TM_T20    = 6
        const val EPOS2_TM_T60    = 7
        const val EPOS2_TM_T70    = 8
        const val EPOS2_TM_T81    = 9
        const val EPOS2_TM_T82    = 10
        const val EPOS2_TM_T83    = 11
        const val EPOS2_TM_T88    = 12
        const val EPOS2_TM_T90    = 13
        const val EPOS2_TM_T90KP  = 14
        const val EPOS2_TM_U220   = 15
        const val EPOS2_TM_U330   = 16
        const val EPOS2_TM_L90    = 17
        const val EPOS2_TM_H6000  = 18
        const val EPOS2_TM_T83III = 19
        const val EPOS2_TM_T100   = 20
        const val EPOS2_TM_M30II  = 21
        const val EPOS2_TS_100    = 22
        const val EPOS2_TM_M50    = 23
        const val EPOS2_TM_T88VII = 24
        const val EPOS2_TM_L90LFC = 25
        const val EPOS2_TM_L100   = 26
        const val EPOS2_TM_P20II  = 27
        const val EPOS2_TM_P80II  = 28
        const val EPOS2_TM_M30III = 29
        const val EPOS2_MODEL_ANK = 0
    }
}
