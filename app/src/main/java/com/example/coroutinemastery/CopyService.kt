package com.example.coroutinemastery

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.coroutinemastery.coroutine_practise.FileCopyService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

class CopyService:Service(),CoroutineScope {
    private val job = Job()
    private val binder = LocalBinder()
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override val coroutineContext: CoroutineContext
        get() = job+Dispatchers.IO
    inner class LocalBinder:Binder(){
        fun getService():CopyService = this@CopyService
    }

    fun copyFile(file:File,destination: File){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
            startForeground(NOTIFICATION_ID,createNotification("copying",0).build(),ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        }else{
            startForeground(NOTIFICATION_ID,createNotification("copying",0).build())
        }
        launch {
            try {
                CopyOperation.copyFile(file,destination){
                    updateNotification((it.progress*100).toInt(),it.speed.toInt())
                }
                updateNotification(100)
                cancelNotification()
                this@CopyService.stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf()
            }catch (ex:Exception){
                Log.e("Error at service",ex.message?:"Unknown")
            }
        }
    }


    private fun createNotification(content: String, progress: Int): NotificationCompat.Builder {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "CopyChannel",
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
    private fun updateNotification(progress: Int,speed:Int) {
        val notification = createNotification("Copying file", progress)
        val n = if (speed>1024) "${speed/1024} MB" else "$speed KB"
        notification.setSubText(n)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }
    private fun cancelNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
    companion object {
        private const val CHANNEL_ID = "CopyChannel"
        private const val NOTIFICATION_ID = 11
    }
}