package de.tillhub.printengine.data

import de.tillhub.printengine.helpers.HashHelper

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
    val serviceVersion: PrinterServiceVersion,
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

    override fun hashCode() = HashHelper.hash(
        serialNumber,
        deviceModel,
        printerVersion,
        printerPaperSpec,
        printingFontType,
        printerHead,
        printedDistance,
        serviceVersion,
    )
}

/**
 * Defines the width of the printing paper.
 */
sealed class PrintingPaperSpec(
    open val characterCount: Int,
) {
    data object PaxPaper56mm : PrintingPaperSpec(PAX_PAPER_56MM_CHAR_COUNT)

    data object SunmiPaper56mm : PrintingPaperSpec(SUNMI_PAPER_56MM_CHAR_COUNT)

    data object VerifonePaper56mm : PrintingPaperSpec(VERIFONE_PAPER_56MM_CHAR_COUNT)

    class External(
        override val characterCount: Int,
    ) : PrintingPaperSpec(characterCount) {
        override fun toString() = "PrintingPaperSpec.External(" +
            "characterCount=$characterCount" +
            ")"

        override fun equals(other: Any?) = other is External &&
            characterCount == other.characterCount

        override fun hashCode() = HashHelper.hash(characterCount)
    }

    companion object {
        const val PAX_PAPER_56MM_CHAR_COUNT = 35
        const val SUNMI_PAPER_56MM_CHAR_COUNT = 38
        const val VERIFONE_PAPER_56MM_CHAR_COUNT = 32
    }
}

/**
 * Defines the font size.
 */
enum class PrintingFontType {
    DEFAULT_FONT_SIZE,
}

/**
 * Represents the version of the devices printer service.
 */
sealed class PrinterServiceVersion {
    data object Unknown : PrinterServiceVersion()

    class Info(
        val serviceVersionName: String,
        val serviceVersionCode: Long,
    ) : PrinterServiceVersion() {
        override fun toString() = "PrinterServiceVersion.Info(" +
            "serviceVersionName=$serviceVersionName, " +
            "serviceVersionCode=$serviceVersionCode" +
            ")"

        override fun equals(other: Any?) = other is Info &&
            serviceVersionName == other.serviceVersionName &&
            serviceVersionCode == other.serviceVersionCode

        override fun hashCode() = HashHelper.hash(
            serviceVersionName,
            serviceVersionCode,
        )
    }
}
