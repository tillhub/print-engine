package de.tillhub.printengine.data

import java.util.Objects

/**
 * [de.tillhub.printengine.Printer] related information.
 */
class PrinterInfo(
    val serialNumber: String,
    val deviceModel: String,
    val printerVersion: String,
    val printerPaperSpec: PrintingPaperSpec,
    val printingFontType: PrintingFontType,
    val printerHead: String,
    val printedDistance: Int,
    val serviceVersion: PrinterServiceVersion
) {
    override fun toString() = "PrinterInfo(" +
            "serialNumber=$serialNumber, " +
            "deviceModel=$deviceModel, " +
            "printerVersion=$printerVersion, " +
            "printerPaperSpec=$printerPaperSpec, " +
            "printingFontType=$printingFontType, " +
            "printerHead=$printerHead, " +
            "printedDistance=$printedDistance, " +
            "serviceVersion=$serviceVersion" +
            ")"

    override fun equals(other: Any?) = other is PrinterInfo &&
            serialNumber == other.serialNumber &&
            deviceModel == other.deviceModel &&
            printerVersion == other.printerVersion &&
            printerPaperSpec == other.printerPaperSpec &&
            printingFontType == other.printingFontType &&
            printerHead == other.printerHead &&
            printedDistance == other.printedDistance &&
            serviceVersion == other.serviceVersion

    override fun hashCode() = Objects.hash(
        serialNumber,
        deviceModel,
        printerVersion,
        printerPaperSpec,
        printingFontType,
        printerHead,
        printedDistance,
        serviceVersion
    )
}

/**
 * Defines the width of the printing paper.
 */
enum class PrintingPaperSpec(val characterCount: Int) {
    /** Printing paper width of 56 mm */
    PAX_PAPER_56MM(characterCount = 32),
    SUNMI_PAPER_56MM(characterCount = 38),
    VERIFONE_PAPER_58MM(characterCount = 32)
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
    data object Unknown : PrinterServiceVersion()
    class Info(
        val serviceVersionName: String,
        val serviceVersionCode: Long
    ) : PrinterServiceVersion() {
        override fun toString() = "PrinterServiceVersion.Info(" +
                "serviceVersionName=$serviceVersionName, " +
                "serviceVersionCode=$serviceVersionCode" +
                ")"

        override fun equals(other: Any?) = other is Info &&
                serviceVersionName == other.serviceVersionName &&
                serviceVersionCode == other.serviceVersionCode

        override fun hashCode() = Objects.hash(
            serviceVersionName,
            serviceVersionCode
        )
    }
}
