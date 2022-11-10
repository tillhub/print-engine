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
import de.tillhub.printengine.data.PrinterConnectionState
import de.tillhub.printengine.data.PrinterServiceVersion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Print service for encapsulating connection handling, error handling and convenience methods for working with
 * [SunmiPrinterController].
 */
class SunmiPrintService : PrintService() {

    override var printController: PrinterController? = null
    private var serviceVersion: PrinterServiceVersion = PrinterServiceVersion.Unknown

    private val connectionState = MutableStateFlow<PrinterConnectionState>(PrinterConnectionState.CheckingForPrinter)
    override val printerConnectionState: StateFlow<PrinterConnectionState> = connectionState

    private val innerPrinterCallback: InnerPrinterCallback = object : InnerPrinterCallback() {
        override fun onConnected(service: SunmiPrinterService) {
            printController = SunmiPrinterController(service, serviceVersion)

            // Check the printer connection, as some devices do not have a printer but need to be connected to the
            // cash drawer through a print service.
            try {
                InnerPrinterManager.getInstance().hasPrinter(service)
            } catch (e: InnerPrinterException) {
                e.printStackTrace()
                false
            }.let {
                connectionState.value = when (it) {
                    true -> PrinterConnectionState.PrinterConnected
                    false -> PrinterConnectionState.PrinterNotAvailable
                }
            }
        }

        override fun onDisconnected() {
            printController = null
            serviceVersion = PrinterServiceVersion.Unknown
            connectionState.value = PrinterConnectionState.PrinterConnectionLost
        }
    }

    override fun initPrinterService(context: Context) {
        try {
            serviceVersion = getServiceVersion(context)
            if (!InnerPrinterManager.getInstance().bindService(context, innerPrinterCallback)) {
                connectionState.value = PrinterConnectionState.PrinterNotAvailable
            }
        } catch (e: InnerPrinterException) {
            e.printStackTrace()
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
            e.printStackTrace()
        }

        return PrinterServiceVersion.Unknown
    }
}