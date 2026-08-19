package de.tillhub.printengine.pax

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import androidx.core.os.bundleOf
import de.tillhub.printengine.pax.DirectPrintService.DirectPrintListener

internal interface DirectPrintService {
    interface DirectPrintListener {
        fun onFailed(e: RemoteException)

        fun onStatus(status: Int)
    }

    fun checkStatus(listener: DirectPrintListener)

    fun print(
        content: String,
        printingIntensity: Int,
        listener: DirectPrintListener,
    )
}

internal class DirectPrintServiceImpl(
    private val requestMessenger: Messenger,
) : DirectPrintService {
    override fun checkStatus(listener: DirectPrintListener) {
        Message
            .obtain(null, MSG_STATUS, 0, 0)
            .apply {
                replyTo = Messenger(PrintResponseHandler(listener))
            }.also { message ->
                // A failed status check only means the state is unknown, so it is reported and
                // swallowed - this runs from the controller's constructor.
                sendMessage(message, listener, rethrowOnFailure = false)
            }
    }

    override fun print(
        content: String,
        printingIntensity: Int,
        listener: DirectPrintListener,
    ) {
        Message
            .obtain(null, MSG_PRINT, 0, 0)
            .apply {
                replyTo = Messenger(PrintResponseHandler(listener))
                data =
                    bundleOf(
                        MSG_PRINT_HTML to content,
                        MSG_PRINT_AUTO_CROP to true,
                        MSG_PRINT_INTENSITY to printingIntensity,
                    )
            }.also { message ->
                sendMessage(message, listener, rethrowOnFailure = true)
            }
    }

    /**
     * @throws RemoteException if [rethrowOnFailure] and the transaction fails.
     *
     * [DirectPrintListener.onFailed] only moves the printer state; it does not tell the caller of
     * `startPrintJob` anything. Rethrowing is what turns a failed transaction - including the
     * `TransactionTooLargeException` an oversized receipt triggers - into a `PrinterResult.Error`
     * instead of a `Success` for a receipt that was never printed.
     */
    private fun sendMessage(
        message: Message,
        listener: DirectPrintListener,
        rethrowOnFailure: Boolean,
    ) {
        try {
            requestMessenger.send(message)
        } catch (e: RemoteException) {
            listener.onFailed(e)
            if (rethrowOnFailure) throw e
        }
    }

    class PrintResponseHandler(
        private val listener: DirectPrintListener,
    ) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val status = msg.data.getInt(PRINTER_STATUS_KEY, PaxPrinterState.NotAvailable.code)
            listener.onStatus(status)
        }
    }

    companion object {
        private const val MSG_PRINT = 1
        private const val MSG_STATUS = 2
        private const val MSG_PRINT_HTML = "html"
        private const val MSG_PRINT_AUTO_CROP = "autoCrop"
        private const val MSG_PRINT_INTENSITY = "grey"
        private const val PRINTER_STATUS_KEY = "status"
    }
}
