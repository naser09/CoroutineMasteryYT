package com.example.coroutinemastery.coroutine_practise

import android.os.Build
import android.os.FileObserver
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

fun callbackFlow(filePath:String,event:(Int)->Unit):Flow<String> {
    return callbackFlow {
        val callback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object :FileObserver(File(filePath)){
                override fun onEvent(event: Int, path: String?) {
                    trySend(path?:"")
                    event(event)
                    if (event== DELETE){
                        Log.d("OnEvent","Delete $event path $path")
                    }
                    Log.d("OnEvent","event $event path $path")
                }

                override fun stopWatching() {
                    super.stopWatching()
                    close()
                }
            }
        } else {
            object :FileObserver(filePath){
                override fun onEvent(event: Int, path: String?) {
                    path?.let { trySend(it) }
                }

                override fun stopWatching() {
                    super.stopWatching()
                    close()
                }
            }
        }
        callback.startWatching()
        awaitClose { callback.stopWatching() }
    }
}