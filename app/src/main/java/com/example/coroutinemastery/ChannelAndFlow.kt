package com.example.coroutinemastery

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking



fun main5():Unit=runBlocking {
    val p = myProducer()
    repeat(5){
        myProcessor(it,p)
    }
//    val channel = Channel<Int>()
//    launch {
//        repeat(5){
//            channel.trySend(it+1)
//            delay(1000)
//        }
//    }
//    launch {
//        delay(3000)
//        channel.close()
//    }
//    for (num in channel){
//        println(num)
//    }
}
@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineScope.myProducer() = produce<Int> {
    repeat(10){
        send(it)
        delay(300)
    }
}
fun CoroutineScope.myProcessor(id:Int,channel: ReceiveChannel<Int>) = launch {
    for (data in channel){
        println("id : $id ----- data: $data")
    }
}