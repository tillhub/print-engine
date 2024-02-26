package de.tillhub.printengine.data

/**
 * Any kind of content or instructions which can be sent directly to the printer.
 */
data class RawPrinterData(
    val bytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawPrinterData

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}