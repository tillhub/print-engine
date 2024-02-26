package de.tillhub.printengine.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    fun iO(): CoroutineDispatcher
}
