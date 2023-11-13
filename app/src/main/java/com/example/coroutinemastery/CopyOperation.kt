package com.example.coroutinemastery

import java.io.File

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
                while (byte>=0){
                    output.write(buffer,0,byte)
                    byte = it.read(buffer)
                    copied+=byte
                    status(
                        Status(
                            progress = copied.toFloat()/fileToCopy.length().toFloat(),
                            speed = byte/1024f //KB
                        )
                    )
                }
                it.close()
            }
    }
}