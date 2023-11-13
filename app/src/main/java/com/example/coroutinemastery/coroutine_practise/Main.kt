package com.example.coroutinemastery.coroutine_practise

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout


fun main2(){
    runBlocking (Dispatchers.Default){
        taskTest()
        val job = launch {
            while (true){
                delay(1000)
                println("launch")
            }
        }
        val result = async {
            delay(1000)
            return@async "hello"
        }
        launch(Dispatchers.IO){
            println(this.coroutineContext)
        }
        try {
            withTimeout(2000){
                delay(3000)
                job.cancelAndJoin()
            }
        }catch (e:Exception){
            println(e.localizedMessage)
        }finally {
            job.cancelAndJoin()
        }
        val r = result.await()
        println(r)
    }
}
suspend fun taskTest(){
    val scope = CoroutineScope(Dispatchers.Default)
    scope.launch {
        repeat(10){
            delay(200)
            println(scope.coroutineContext)
        }
    }
    delay(1000)
    scope.cancel()
}