package de.tillhub.printengine.epson

import android.content.Context
import com.epson.epos2.discovery.DeviceInfo
import com.epson.epos2.discovery.Discovery
import com.epson.epos2.discovery.FilterOption


interface DiscoveryWrapper {
    fun start(
        context: Context,
        filterOption: FilterOption,
        callback: (DeviceInfo) -> Unit
    )
    fun stop()
}

class EpsonDiscoveryWrapper : DiscoveryWrapper {
    override fun start(
        context: Context,
        filterOption: FilterOption,
        callback: (DeviceInfo) -> Unit
    ) = Discovery.start(context, filterOption) { deviceInfo -> callback(deviceInfo) }

    override fun stop() = Discovery.stop()
}