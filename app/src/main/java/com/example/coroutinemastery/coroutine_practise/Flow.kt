package com.example.coroutinemastery.coroutine_practise

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MyFlow(){
    operator fun invoke():Flow<Long>{
        return flow {
            while (true){
                delay(1000)
                emit(System.currentTimeMillis())
            }
        }
    }
}
data class Student(
    val tole:Int,
    val name:String
)

fun main6() = runBlocking{
    launch {
        delay(1000)
        getUserApi.sentData()
        delay(1000)
        getUserApi.throwErr()
    }
    getUser().catch {
        println("Catched ${it.message}")
    }.collect{
        println("collected $it")
    }
    flowOf(1,2,3).collectLatest {
        println(it)
    }
    val error = flow {
        repeat(6){
            try {
                if (it!=4) delay(200)
                if (it==3) throw Exception("Test Exception")
                emit(it)
            }catch (ex:Exception){
                println("Exception $it")
            }
        }
    }
    error.catch {
     println("error ${it.message}")
    }.onCompletion {
        println("onCompletion")
        println(it?.localizedMessage?:"Success")
    }.onEach {
     println("onEach $it")
    }.collectLatest {
        println("latest = $it")
    }
//    val f = listOf("a","d","d","f").asFlow()
//    (1..1000).asFlow().combine(f) { s, i ->
//        Student(s , i)
//    }.collect{
//        println(it)
//    }
    (1..5).asFlow()
        .flatMapMerge {
            double(it)
        }.collect{
        println(it)
    }
    val nums = (1..10).asFlow()
        .onEach { delay(29) }
    val strs = flowOf('a', 'b', 'c', 'd')
        .onEach { delay(37) }
    nums.zip(strs) { a, b -> "$a -> $b" }
        .collect {
            println(it)
        }
    (1..2).asFlow()
        .onEach { println("onEach1: $it is on ${Thread.currentThread().name}") }
        .onEach { println("onEach2: $it is on ${Thread.currentThread().name}") }
        .flowOn(Dispatchers.Default)
        .onEach { println("onEach3: $it is on ${Thread.currentThread().name}") }
        .collect { println("collect: $it is on ${Thread.currentThread().name}") }

}
fun double(value: Int) = flow {
    emit(value.toDouble())
    delay(100)
    emit(value.toDouble())
}

fun getUser(): Flow<String> = callbackFlow {
    val callback = object : GetUserCallback {
        override fun onNextValue(value: String) {
           trySendBlocking(value).onFailure {
               throw Exception("onFailure Called")
           }
        }
        override fun onApiError(cause: Throwable) {
            close(cause)
        }
        override fun onCompleted() {
            close()
        }
    }
    getUserApi.register(callback)
    awaitClose { getUserApi.unregister(callback) }
}
val getUserApi = UserApi()
class UserApi{
    fun sentData(){
        (1..10).forEach { char ->
            callback?.let {
                it.onNextValue(char.toString())
            }
        }
    }
    fun throwErr(){
        callback?.onApiError(Throwable("Test"))
    }
    private var callback: GetUserCallback?=null
    fun register(callback: GetUserCallback){
        this.callback = callback
    }
    fun unregister(callback: GetUserCallback){
        this.callback = callback
    }
}
interface GetUserCallback{
     fun onNextValue(value: String)
     fun onApiError(cause: Throwable)
     fun onCompleted()
}