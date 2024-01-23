package de.tillhub.printengine.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class DispatcherProviderImp : DispatcherProvider {
    override fun iO(): CoroutineDispatcher = Dispatchers.IO
}
