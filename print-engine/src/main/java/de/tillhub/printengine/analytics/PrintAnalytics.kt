package de.tillhub.printengine.analytics

interface PrintAnalytics {
    fun logPrintReceipt(receiptText: String)
    fun logErrorPrintReceipt(message: String)
}
