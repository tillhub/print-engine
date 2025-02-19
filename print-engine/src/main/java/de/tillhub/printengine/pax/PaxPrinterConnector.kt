package de.tillhub.printengine.pax

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import de.tillhub.printengine.data.PrinterState
import kotlinx.coroutines.flow.MutableStateFlow

internal class PaxPrinterConnector(
    private val context: Context,
    private val printerState: MutableStateFlow<PrinterState>,
) {
    fun sendPrintRequest(
        payload: String,
        printingIntensity: Int,
        responseHandler: PrintResponseHandler
    ) {
        val message = Message.obtain(null, PRINTING_REQUEST, 0, 0).apply {
            replyTo = Messenger(responseHandler)
            data = Bundle().apply {
                putString("html", payload)
                putBoolean("autoCrop", true)
                putInt("grey", printingIntensity)
            }
        }

        sendMessage(message)
    }

    fun sendStatusRequest(responseHandler: PrintResponseHandler) {
        val message = Message.obtain(null, STATUS_REQUEST, 0, 0).apply {
            replyTo = Messenger(responseHandler)
        }

        sendMessage(message)
    }

    private fun sendMessage(message: Message) {
        val intent = Intent().apply {
            component = ComponentName(PRINTING_PACKAGE, PRINTING_CLASS)
        }

        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                printerState.value = PrinterState.Busy

                val printMessenger = Messenger(service)
                printMessenger.send(message)
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }

        if (!context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            context.unbindService(connection)
            printerState.value = PrinterState.Error.NotAvailable
        }
    }

    companion object {
        private const val PRINTING_PACKAGE = "de.ccv.payment.printservice"
        private const val PRINTING_CLASS = "de.ccv.payment.printservice.DirectPrintService"

        private const val PRINTING_REQUEST = 1
        private const val STATUS_REQUEST = 2
    }
}