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
 *
 * [characterCount] describes the paper in text terms, [printHeadWidthPx] in dots. Both are
 * needed: text is laid out per character, while images have to be scaled to what the head can
 * physically resolve.
 */
sealed class PrintingPaperSpec(
    open val characterCount: Int,
    open val printHeadWidthPx: Int,
) {
    data object PaxPaper56mm : PrintingPaperSpec(
        characterCount = PAX_PAPER_56MM_CHAR_COUNT,
        printHeadWidthPx = PAPER_56MM_HEAD_WIDTH_PX,
    )

    data object SunmiPaper56mm : PrintingPaperSpec(
        characterCount = SUNMI_PAPER_56MM_CHAR_COUNT,
        printHeadWidthPx = PAPER_56MM_HEAD_WIDTH_PX,
    )

    data object VerifonePaper56mm : PrintingPaperSpec(
        characterCount = VERIFONE_PAPER_56MM_CHAR_COUNT,
        printHeadWidthPx = PAPER_56MM_HEAD_WIDTH_PX,
    )

    class External(
        override val characterCount: Int,
        override val printHeadWidthPx: Int = EXTERNAL_HEAD_WIDTH_PX,
    ) : PrintingPaperSpec(characterCount, printHeadWidthPx) {
        override fun toString() = "PrintingPaperSpec.External(" +
            "characterCount=$characterCount, " +
            "printHeadWidthPx=$printHeadWidthPx" +
            ")"

        override fun equals(other: Any?) = other is External &&
            characterCount == other.characterCount &&
            printHeadWidthPx == other.printHeadWidthPx

        override fun hashCode() = HashHelper.hash(characterCount, printHeadWidthPx)
    }

    companion object {
        const val PAX_PAPER_56MM_CHAR_COUNT = 35
        const val SUNMI_PAPER_56MM_CHAR_COUNT = 38
        const val VERIFONE_PAPER_56MM_CHAR_COUNT = 32

        /** 56mm of printable width at the 203 dpi every built-in head in this library runs at. */
        const val PAPER_56MM_HEAD_WIDTH_PX = 384

        /** 80mm at 203 dpi, the common width of the external receipt printers we support. */
        const val EXTERNAL_HEAD_WIDTH_PX = 576
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
