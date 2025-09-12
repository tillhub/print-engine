package de.tillhub.printengine.data

/**
 * Any kind of content or instructions which can be sent directly to the printer.
 */
class RawPrinterData(
    val bytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawPrinterData

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    override fun toString() = "RawPrinterData(" +
            "bytes=$bytes" +
            ")"
}
