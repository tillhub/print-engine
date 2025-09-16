package de.tillhub.printengine.data

/**
 * Any kind of content or instructions which can be sent directly to the printer.
 */
class RawPrinterData(
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (this::class != other::class) return false

        other as RawPrinterData

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun toString() = "RawPrinterData(" +
        "bytes=$bytes" +
        ")"
}
