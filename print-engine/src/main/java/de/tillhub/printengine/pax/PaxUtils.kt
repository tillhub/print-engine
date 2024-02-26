package de.tillhub.printengine.pax

object PaxUtils {

    // Max buffer size for printing is 2048 bytes.
    // Max lines * paper width (32) should not go over the buffer size limit
    private const val MAX_PRINT_LINES = 50

    /**
     * It fixes issues with pax printer printing non ASCII characters.
     */
    fun printTextOptimizer(text: String): String {
        return text.replace("\\s€".toRegex(), "€")
    }

    /**
     * It splits text into chunks by chunkSize
     */
    fun chunkForPrinting(text: String, chunkSize: Int = MAX_PRINT_LINES): List<String> {
        val lines = text.split('\n')

        return mutableListOf<String>().apply {
            val sb = StringBuilder()
            lines.forEachIndexed { index, s ->
                sb.append(s)
                sb.append("\n")
                if (index == (lines.size - 1) || (index + 1) % chunkSize == 0) {
                    add(sb.toString())
                    sb.clear()
                }
            }
        }
    }

    /**
     * Centers content in space width.
     */
    fun formatCode(content: String, space: Int): String {
        return if (space < content.length) {
            content.take(space)
        } else {
            val padStart = (space - content.length) / 2
            (" ".repeat(padStart) + content).padEnd(space)
        }
    }
}