package com.example.coroutinemastery

import kotlinx.coroutines.delay
import java.io.File
import kotlin.random.Random

object CopyOperation {
    suspend fun copyFile(fileToCopy:File, pasteDirectory:File, status:suspend (Status)->Unit){
            val input = fileToCopy.inputStream()
            val file = if (pasteDirectory.isDirectory){
                File(pasteDirectory.absolutePath,fileToCopy.name)
            }else{
                pasteDirectory
            }
            if (file.exists()) file.delete()
            val output = file.outputStream()
            input.use {
                var copied = 0
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var byte = it.read(buffer)
                var beforeTime = System.currentTimeMillis()
                var speed= 0
                while (byte>=0){
                    output.write(buffer,0,byte)
                    byte = it.read(buffer)
                    copied+=byte
                    if (System.currentTimeMillis()-beforeTime>=1000){
                        status(
                            Status(
                                progress = copied.toFloat()/fileToCopy.length().toFloat(),
                                speed = speed/1024f //KB
                            )
                        )
                        speed=0
                        beforeTime = System.currentTimeMillis()
                    }else{
                        speed+=byte
                    }
                }
                status(
                    Status(
                        progress = copied.toFloat()/fileToCopy.length().toFloat(),
                        speed = speed/1024f //KB
                    )
                )
                it.close()
            }
    }
}