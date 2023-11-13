package com.example.coroutinemastery

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

fun main1():Unit = runBlocking {
    val channel: Channel<Map<Int,String>> = Channel(Channel.Factory.UNLIMITED,BufferOverflow.DROP_OLDEST)
    val supervisor = supervisorScope {
        launch {
            delay(1000)
            val map = mapOf(1 to "first")
            channel.send(map)
        }
    }
    launch {
        delay(2000)
        val map = mapOf(2 to "second")
        channel.send(map)
        channel.close()
    }
    for (data in channel){
        println(data)
    }
    val prodcer = producer()
    launch { for (n in 0..10){ processor(n,prodcer) } }
    launch { for (n in 11..20){ processor(n,prodcer) } }
    delay(5000)
    prodcer.cancel()
}

@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineScope.producer() = produce<Int> {
    var i=1
    while (true){
        send(i++)
        delay(200)
    }
}
fun CoroutineScope.processor(id: Int,channel:ReceiveChannel<Int>) = launch {
    for (num in channel){
        println("id = $id num = $num")
    }
}