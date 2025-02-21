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
    fun print(content: String, printingIntensity: Int, listener: DirectPrintListener)
}

internal class DirectPrintServiceImpl(private val requestMessenger: Messenger) : DirectPrintService {

    override fun checkStatus(listener: DirectPrintListener) {
        Message.obtain(null, MSG_STATUS, 0, 0).apply {
            replyTo = Messenger(PrintResponseHandler(listener))
        }.also { message ->
            sendMessage(message, listener)
        }
    }

    override fun print(content: String, printingIntensity: Int, listener: DirectPrintListener) {
        Message.obtain(null, MSG_PRINT, 0, 0).apply {
            replyTo = Messenger(PrintResponseHandler(listener))
            data = bundleOf(
                MSG_PRINT_HTML to content,
                MSG_PRINT_AUTO_CROP to true,
                MSG_PRINT_INTENSITY to printingIntensity
            )
        }.also { message ->
            sendMessage(message, listener)
        }
    }

    private fun sendMessage(message: Message, listener: DirectPrintListener) {
        try {
            requestMessenger.send(message)
        } catch (e: RemoteException) {
            listener.onFailed(e)
        }
    }

    class PrintResponseHandler(
        private val listener: DirectPrintListener
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