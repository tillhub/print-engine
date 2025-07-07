package de.tillhub.printengine.sunmi

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterException
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.SunmiPrinterService
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

/**
 * Print service for encapsulating connection handling, error handling and convenience methods for working with
 * [SunmiPrinterController].
 */
internal class SunmiPrintService(context: Context) : PrintService() {

    override var printController: PrinterController? = null
    private var serviceVersion: PrinterServiceVersion = PrinterServiceVersion.Unknown

    private val connectionState = MutableStateFlow<PrinterState>(PrinterState.CheckingForPrinter)
    override val printerState: Flow<PrinterState> = connectionState

    private val innerPrinterCallback: InnerPrinterCallback = object : InnerPrinterCallback() {
        override fun onConnected(service: SunmiPrinterService) {
            printController = SunmiPrinterController(service, serviceVersion, connectionState)

            // Check the printer connection, as some devices do not have a printer but need to be connected to the
            // cash drawer through a print service.
            try {
                InnerPrinterManager.getInstance().hasPrinter(service)
            } catch (e: InnerPrinterException) {
                Timber.e(e)
                false
            }.let {
                connectionState.value = when (it) {
                    true -> PrinterState.Connected
                    false -> PrinterState.Error.NotAvailable
                }
            }
        }

        override fun onDisconnected() {
            printController = null
            serviceVersion = PrinterServiceVersion.Unknown
            connectionState.value = PrinterState.Error.ConnectionLost
        }
    }

    init {
        try {
            serviceVersion = getServiceVersion(context)
            if (!InnerPrinterManager.getInstance().bindService(context, innerPrinterCallback)) {
                connectionState.value = PrinterState.Error.NotAvailable
            }
            Timber.i("printing: SunmiPrintService initialized")
        } catch (e: InnerPrinterException) {
            Timber.e(e)
        }
    }

    private fun getServiceVersion(context: Context): PrinterServiceVersion {
        try {
            val packageInfo = context.packageManager.getPackageInfo("woyou.aidlservice.jiuiv5", 0)
            if (packageInfo != null) {
                return PrinterServiceVersion.Info(
                    packageInfo.versionName,
                    PackageInfoCompat.getLongVersionCode(packageInfo)
                )
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
        }

        return PrinterServiceVersion.Unknown
    }
}
