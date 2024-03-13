package de.tillhub.printengine.data

/**
 * [de.tillhub.printengine.Printer] related information.
 */
data class PrinterInfo(
    val serialNumber: String,
    val deviceModel: String,
    val printerVersion: String,
    val printerPaperSpec: PrintingPaperSpec,
    val printingFontType: PrintingFontType,
    val printerHead: String,
    val printedDistance: Int,
    val serviceVersion: PrinterServiceVersion
)

/**
 * Defines the width of the printing paper.
 */
enum class PrintingPaperSpec(val characterCount: Int) {
    /** Printing paper width of 56 mm */
    PAX_PAPER_56MM(32),
    SUNMI_PAPER_56MM(38),
    VERIFONE_PAPER_58MM(32)
}

/**
 * Defines the font size.
 */
enum class PrintingFontType {
    DEFAULT_FONT_SIZE
}

/**
 * Represents the version of the devices printer service.
 */
sealed class PrinterServiceVersion {
    object Unknown : PrinterServiceVersion()
    data class Info(
        val serviceVersionName: String,
        val serviceVersionCode: Long
    ) : PrinterServiceVersion()
}