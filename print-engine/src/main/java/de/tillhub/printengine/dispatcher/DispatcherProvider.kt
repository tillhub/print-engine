package de.tillhub.printengine.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

internal interface DispatcherProvider {
    fun iO(): CoroutineDispatcher
}
