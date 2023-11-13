package com.example.coroutinemastery

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun main():Unit = runBlocking{
    //flow builder
    val flow1: Flow<Int> = flowOf(1,2,3,4,5,6,7,8,9,10)
    val flow2: Flow<Int> = listOf(11,12,13,14).asFlow()
    val flow3 = listOf(14..20).asFlow()
    val flow4:Flow<Int> = flow {
        repeat(5){
            try {
                delay(1000)
                if (it==3) throw Exception("Error")
                emit(it)
                println(Thread.currentThread().name)
            }catch (ex:Exception){

            }
        }
    }
    //intermediate operator
    flow4.onEach {
        println(it)
    }.catch {
        println(it.localizedMessage)
    }.onCompletion {
        emit(10)
    }.collect{
        println(it)
    }
    //terminal operator
}
fun data(value:Int)= flow {
    delay(500)
    emit("from function $value")
    delay(500)
    emit("flow ended $value")
}