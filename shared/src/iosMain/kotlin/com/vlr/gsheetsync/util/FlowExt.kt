package com.vlr.gsheetsync.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

interface Cancellable {
    fun cancel()
}

fun <T> StateFlow<T>.collectOnMain(block: (T) -> Unit): Cancellable {
    val job = Job()
    
    CoroutineScope(Dispatchers.Default + job).launch {
        collect { value ->
            dispatch_async(dispatch_get_main_queue()) {
                block(value)
            }
        }
    }
    
    return object : Cancellable {
        override fun cancel() {
            job.cancel()
        }
    }
}

// Extension function to be called from Swift
fun <T> StateFlow<T>.watchState(block: (T) -> Unit): Cancellable = collectOnMain(block) 