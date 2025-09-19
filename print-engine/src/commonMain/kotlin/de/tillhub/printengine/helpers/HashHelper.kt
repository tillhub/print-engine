package de.tillhub.printengine.helpers

object HashHelper {
    fun hash(vararg vals: Any?): Int {
        var res = 0
        for (v in vals) {
            res += v.hashCode()
            res *= HASH_OFFSET
        }
        return res
    }

    private const val HASH_OFFSET = 31
}
