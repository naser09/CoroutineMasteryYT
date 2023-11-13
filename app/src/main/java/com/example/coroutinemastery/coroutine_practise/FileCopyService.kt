package com.example.coroutinemastery.coroutine_practise

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class FileCopyService: Service(),CoroutineScope{
    private val job = Job()
    private val binder = LocalBinder()
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override val coroutineContext: CoroutineContext
        get() = job+Dispatchers.IO

    inner class LocalBinder : Binder() {
        fun getService(): FileCopyService = this@FileCopyService
    }
    fun startFileCopy(sourcePath: File, destinationPath: File) {
        startForeground(NOTIFICATION_ID, createNotification("Copying file", 0).build())
        launch {
            try {
                copyFile(sourcePath, destinationPath)
                this@FileCopyService.stopForeground(STOP_FOREGROUND_DETACH)
//                stopForeground(true)
                stopSelf()
            } catch (e: IOException) {
                // Handle error
            }
        }
    }
    private suspend fun copyFile(sourceFile: File, destinationFile: File) {
        Log.d("Copy File "," source ${sourceFile.absolutePath} destination ${destinationFile.absolutePath}")
        val fileSize = sourceFile.length()
        withContext(Dispatchers.IO){
            val file = if (destinationFile.isDirectory){
                File(destinationFile.absolutePath,sourceFile.name)
            }else{
                destinationFile
            }
            if (file.exists()){
                file.delete()
            }
            file.createNewFile()
            val input = sourceFile.inputStream()
            val output = file.outputStream()
//            while (input.read(buffer).also { bytesRead = it } > 0) {
//                output.write(buffer, 0, bytesRead)
//                totalBytesRead += bytesRead.toLong()
//                // Update progress notification
//                updateNotification((totalBytesRead * 100 / fileSize).toInt())
//            }
//            input.close()
//            output.close()
            input.use {
                var copied = 0
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var byte = it.read(buffer)
                while (byte>=0){
                    output.write(buffer,0,byte)
                    byte = it.read(buffer)
                    copied+=byte
                    Log.d("Service","Copied $copied")
                    updateNotification((copied * 100 / fileSize).toInt())
                }
                it.close()
                updateNotification(100)
                cancelNotification()
            }
        }
    }

    private fun createNotification(content: String, progress: Int): NotificationCompat.Builder {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "FileCopyChannel",
                    NotificationManager.IMPORTANCE_LOW
                )
                notificationManager.createNotificationChannel(channel)
            }

            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("File Copy")
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_menu_upload)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, progress, false)
        }

        private fun updateNotification(progress: Int) {
            val notification = createNotification("Copying file", progress)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification.build())
        }
    private fun cancelNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
        companion object {
            private const val CHANNEL_ID = "FileCopyChannel"
            private const val NOTIFICATION_ID = 1
        }
}