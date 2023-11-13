package com.example.coroutinemastery

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun main8(){
    runBlocking {
        val job = launch {
            unstoppable()
        }
        delay(1000)
        job.cancel()
    }
}
suspend fun mySuspendFun():Int{
    println("Hello")
    return 0
}
suspend fun unstoppable(){
    while (true){
        println("running")
        delay(500)
    }
}
suspend fun myDelay(timeMillis:Long){
    if (timeMillis <= 0) return // don't delay
    return suspendCoroutine { cont->
        // if timeMillis == Long.MAX_VALUE then just wait forever like awaitCancellation, don't schedule.
        executor.schedule({
            cont.resume(Unit)
        },timeMillis,TimeUnit.MILLISECONDS)
    }
}
val executor = Executors.newSingleThreadScheduledExecutor {
    Thread(it,"MyThread").apply { isDaemon = true }
}