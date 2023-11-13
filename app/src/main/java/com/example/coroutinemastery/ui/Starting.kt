package com.example.coroutinemastery.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalCoroutinesApi::class)
fun main7() = runBlocking {
    val job = launch(start = CoroutineStart.LAZY) {
        delay(1000)
        println("Naser's CodeLab in coroutine launch")
    }
    val result = async {
        println("Naser's CodeLab in coroutine")
        delay(1000)
        "Naser"
    }
    println("Naser's CodeLab")
    result.cancel()
    job.start()
    job.join()
    val r = result.getCompletionExceptionOrNull()?:result.getCompleted()
    println(r)
}