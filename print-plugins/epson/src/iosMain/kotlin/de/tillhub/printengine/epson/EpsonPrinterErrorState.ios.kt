package de.tillhub.printengine.epson

import com.epson.epos2.Epos2PrinterStatusInfo
import de.tillhub.printengine.data.PrinterState
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
internal object EpsonPrinterErrorState {

    // ePOS2.h error codes (Epos2ErrorStatus enum values)
    private const val EPOS2_SUCCESS          = 0
    private const val EPOS2_ERR_PARAM        = 1
    private const val EPOS2_ERR_CONNECT      = 2
    private const val EPOS2_ERR_TIMEOUT      = 3
    private const val EPOS2_ERR_MEMORY       = 4
    private const val EPOS2_ERR_DISCONNECT   = 10

    // ePOS2.h callback codes (Epos2CallbackCode enum values)
    private const val EPOS2_CODE_SUCCESS  = 0
    private const val EPOS2_CODE_PRINTING = 1

    // ePOS2.h status constants
    private const val EPOS2_FALSE           = 0
    private const val EPOS2_TRUE            = 1
    private const val EPOS2_PAPER_EMPTY     = 2   // EPOS2_PAPER_OK=0, NEAR_END=1, EMPTY=2
    private const val EPOS2_MECHANICAL_ERR  = 0   // Epos2ErrorStatusDetail enum
    private const val EPOS2_AUTOCUTTER_ERR  = 1
    private const val EPOS2_UNRECOVER_ERR   = 2
    private const val EPOS2_AUTORECOVER_ERR = 3
    private const val EPOS2_BATTERY_LEVEL_0 = 0
    private const val EPOS2_HEAD_OVERHEAT   = 0   // Epos2AutoRecoverError enum
    private const val EPOS2_MOTOR_OVERHEAT  = 1
    private const val EPOS2_BATTERY_OVERHEAT = 2
    private const val EPOS2_WRONG_PAPER     = 3
    private const val EPOS2_COVER_OPEN      = 4
    private const val EPOS2_LOW_VOLTAGE_ERR = 0   // Epos2UnrecoverError enum

    fun epsonExceptionToState(e: Exception): PrinterState = when {
        e.message?.contains("ERR_PARAM")       == true -> PrinterState.Error.Epson.DataError
        e.message?.contains("ERR_CONNECT")     == true -> PrinterState.Error.NotAvailable
        e.message?.contains("ERR_TIMEOUT")     == true -> PrinterState.Error.ConnectionLost
        e.message?.contains("ERR_DISCONNECT")  == true -> PrinterState.Error.ConnectionLost
        e.message?.contains("ERR_MEMORY")      == true -> PrinterState.Error.Epson.MemoryError
        else -> PrinterState.Error.Epson.InternalError
    }

    fun epsonStatusToState(code: Int, status: Epos2PrinterStatusInfo?): PrinterState =
        when (code) {
            EPOS2_CODE_SUCCESS  -> PrinterState.Connected
            EPOS2_CODE_PRINTING -> PrinterState.Busy
            else -> if (status != null) evaluateStatusInfo(status) else PrinterState.Error.Epson.InternalError
        }

    private fun evaluateStatusInfo(status: Epos2PrinterStatusInfo): PrinterState = when {
        status.online == EPOS2_FALSE           -> PrinterState.Error.ConnectionLost
        status.paper  == EPOS2_PAPER_EMPTY     -> PrinterState.Error.OutOfPaper
        status.coverOpen == EPOS2_TRUE         -> PrinterState.Error.CoverNotClosed
        status.errorStatus == EPOS2_MECHANICAL_ERR  -> PrinterState.Error.Malfunctions
        status.errorStatus == EPOS2_AUTOCUTTER_ERR  -> PrinterState.Error.PaperCutterAbnormal
        status.errorStatus == EPOS2_UNRECOVER_ERR   -> handleUnrecoverableError(status)
        status.errorStatus == EPOS2_AUTORECOVER_ERR -> handleAutoRecoverableError(status)
        status.batteryLevel == EPOS2_BATTERY_LEVEL_0 -> PrinterState.Error.VoltageTooLow
        else -> PrinterState.Connected
    }

    private fun handleUnrecoverableError(status: Epos2PrinterStatusInfo): PrinterState =
        when (status.unrecoverError) {
            EPOS2_LOW_VOLTAGE_ERR -> PrinterState.Error.VoltageTooLow
            else                  -> PrinterState.Error.Malfunctions
        }

    private fun handleAutoRecoverableError(status: Epos2PrinterStatusInfo): PrinterState =
        when (status.autoRecoverError) {
            EPOS2_HEAD_OVERHEAT,
            EPOS2_MOTOR_OVERHEAT,
            EPOS2_BATTERY_OVERHEAT -> PrinterState.Error.Overheated
            EPOS2_WRONG_PAPER      -> PrinterState.Error.PaperAbnormal
            EPOS2_COVER_OPEN       -> PrinterState.Error.CoverNotClosed
            else                   -> PrinterState.Error.Malfunctions
        }
}
