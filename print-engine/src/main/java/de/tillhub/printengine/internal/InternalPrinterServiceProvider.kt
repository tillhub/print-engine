package de.tillhub.printengine.internal

import android.content.Context
import de.tillhub.printengine.PrintService

interface InternalPrinterServiceProvider {
    fun build(context: Context): PrintService
}