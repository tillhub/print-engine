package de.tillhub.printengine.helper

import androidx.annotation.Keep

open class SingletonHolder<out T : Any>(creator: () -> T) {
    private var creator: (() -> T)? = creator
    @Volatile private var instance: T? = null

    @Keep
    fun getInstance(): T {
        val checkInstance = instance
        if (checkInstance != null) {
            return checkInstance
        }

        return synchronized(this) {
            val checkInstanceAgain = instance
            if (checkInstanceAgain != null) {
                checkInstanceAgain
            } else {
                val created = creator!!()
                instance = created
                creator = null
                created
            }
        }
    }
}
