package com.example.todoapp

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri

class MyApp : Application() {

    companion object{
        const val ABOUTURL= "https://yesterdaylz.github.io/"
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        createReminderChannel()
    }

    private fun createReminderChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri: Uri = "android.resource://${packageName}/${R.raw.todo_reminder}".toUri()
            val audioAttributes = AudioAttributes.Builder()
                //设置音频用途为通知音
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                //设置内容类型为声音提示
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                "todo_reminder_channel",
                getString(R.string.notification),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_description)
                setSound(soundUri, audioAttributes)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}