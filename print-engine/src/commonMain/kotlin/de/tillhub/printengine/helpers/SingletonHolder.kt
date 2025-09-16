package de.tillhub.printengine.helpers

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.Volatile

@OptIn(InternalCoroutinesApi::class)
open class SingletonHolder<out T : Any>(
    creator: () -> T,
) : SynchronizedObject() {
    private var creator: (() -> T)? = creator

    @Volatile
    private var instance: T? = null

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
