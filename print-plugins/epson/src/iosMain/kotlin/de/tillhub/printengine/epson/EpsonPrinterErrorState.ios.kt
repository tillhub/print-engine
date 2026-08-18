package de.tillhub.printengine.epson

import com.epson.epos2.EPOS2_AUTOCUTTER_ERR
import com.epson.epos2.EPOS2_AUTORECOVER_ERR
import com.epson.epos2.EPOS2_BATTERY_LEVEL_0
import com.epson.epos2.EPOS2_BATTERY_OVERHEAT
import com.epson.epos2.EPOS2_CODE_PRINTING
import com.epson.epos2.EPOS2_CODE_SUCCESS
import com.epson.epos2.EPOS2_COVER_OPEN
import com.epson.epos2.EPOS2_ERR_CONNECT
import com.epson.epos2.EPOS2_ERR_DISCONNECT
import com.epson.epos2.EPOS2_ERR_MEMORY
import com.epson.epos2.EPOS2_ERR_PARAM
import com.epson.epos2.EPOS2_ERR_TIMEOUT
import com.epson.epos2.EPOS2_FALSE
import com.epson.epos2.EPOS2_HEAD_OVERHEAT
import com.epson.epos2.EPOS2_MECHANICAL_ERR
import com.epson.epos2.EPOS2_MOTOR_OVERHEAT
import com.epson.epos2.EPOS2_NO_ERR
import com.epson.epos2.EPOS2_PAPER_EMPTY
import com.epson.epos2.EPOS2_TRUE
import com.epson.epos2.EPOS2_UNKNOWN
import com.epson.epos2.EPOS2_UNRECOVER_ERR
import com.epson.epos2.EPOS2_WRONG_PAPER
import com.epson.epos2.Epos2PrinterStatusInfo
import de.tillhub.printengine.data.PrinterState
import kotlinx.cinterop.ExperimentalForeignApi

// Epos2UnrecoverError enum values — not commonized by cinterop across iOS targets
private const val EPOS2_HIGH_VOLTAGE_ERR = 0
private const val EPOS2_LOW_VOLTAGE_ERR = 1

@OptIn(ExperimentalForeignApi::class)
internal object EpsonPrinterErrorState {

    fun epsonErrorStatusToState(errorCode: Int): PrinterState = when (errorCode) {
        EPOS2_ERR_PARAM -> PrinterState.Error.Epson.DataError
        EPOS2_ERR_CONNECT -> PrinterState.Error.NotAvailable
        EPOS2_ERR_TIMEOUT -> PrinterState.Error.ConnectionLost
        EPOS2_ERR_DISCONNECT -> PrinterState.Error.ConnectionLost
        EPOS2_ERR_MEMORY -> PrinterState.Error.Epson.MemoryError
        else -> PrinterState.Error.Epson.InternalError
    }

    fun epsonStatusToState(code: Int, status: Epos2PrinterStatusInfo?): PrinterState = when (code) {
        EPOS2_CODE_SUCCESS -> PrinterState.Connected
        EPOS2_CODE_PRINTING -> PrinterState.Busy
        else -> if (status != null) evaluateStatusInfo(status) else PrinterState.Error.Epson.InternalError
    }

    private fun evaluateStatusInfo(status: Epos2PrinterStatusInfo): PrinterState = when {
        status.online == EPOS2_FALSE -> PrinterState.Error.ConnectionLost
        status.paper == EPOS2_PAPER_EMPTY -> PrinterState.Error.OutOfPaper
        status.coverOpen == EPOS2_TRUE -> PrinterState.Error.CoverNotClosed
        status.errorStatus == EPOS2_NO_ERR -> PrinterState.Connected
        status.errorStatus == EPOS2_MECHANICAL_ERR ||
            status.errorStatus == EPOS2_UNKNOWN -> PrinterState.Error.Malfunctions
        status.errorStatus == EPOS2_AUTOCUTTER_ERR -> PrinterState.Error.PaperCutterAbnormal
        status.errorStatus == EPOS2_UNRECOVER_ERR -> handleUnrecoverableError(status)
        status.errorStatus == EPOS2_AUTORECOVER_ERR -> handleAutoRecoverableError(status)
        status.batteryLevel == EPOS2_BATTERY_LEVEL_0 -> PrinterState.Error.VoltageTooLow
        else -> PrinterState.Connected
    }

    private fun handleUnrecoverableError(status: Epos2PrinterStatusInfo): PrinterState = when (status.unrecoverError) {
        EPOS2_LOW_VOLTAGE_ERR,
        EPOS2_HIGH_VOLTAGE_ERR,
        -> PrinterState.Error.VoltageTooLow
        else -> PrinterState.Error.Malfunctions
    }

    private fun handleAutoRecoverableError(status: Epos2PrinterStatusInfo): PrinterState = when (status.autoRecoverError) {
        EPOS2_HEAD_OVERHEAT,
        EPOS2_MOTOR_OVERHEAT,
        EPOS2_BATTERY_OVERHEAT,
        -> PrinterState.Error.Overheated
        EPOS2_WRONG_PAPER -> PrinterState.Error.PaperAbnormal
        EPOS2_COVER_OPEN -> PrinterState.Error.CoverNotClosed
        else -> PrinterState.Error.Malfunctions
    }
}
