package de.tillhub.printengine.sunmi

import com.sunmi.peripheral.printer.InnerResultCallback

/**
 * Callback for obtaining a [String] value from a Sunmi printer (i.e. printer status, etc.).
 */
abstract class SunmiInnerResultCallback : InnerResultCallback() {
    final override fun onRunResult(isSuccess: Boolean) {
        // not needed
    }

    final override fun onPrintResult(code: Int, msg: String?) {
        // not needed
    }
}