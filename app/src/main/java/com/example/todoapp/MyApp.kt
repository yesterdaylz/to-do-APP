package com.example.todoapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createReminderChannel()
    }

    private fun createReminderChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri: Uri = "android.resource://${packageName}/${R.raw.todo_reminder}".toUri()
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION) // 或 USAGE_ALARM
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                "todo_reminder_channel",
                "待办提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "待办事项截止/提醒通知"
                setSound(soundUri, audioAttributes)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}