package com.example.coroutinemastery

sealed class Resource (val status: Status? = null , val message:String?=null){
    object NotStarted:Resource()
    class Running(status: Status):Resource(status)
    class Success(message: String):Resource(message = message)
}
data class Status(
    val progress:Float,
    val speed:Float,
)