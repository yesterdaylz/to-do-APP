package com.example.todoapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.todoapp.data.entity.Todo
import com.example.todoapp.logic.TodoReminderReceiver

fun scheduleReminder(context: Context, todo: Todo) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            Intent().apply {
                action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(this)
             }
            return
        }
    }
    fun setAlarm(timeMillis: Long, requestCodeOffset: Int) {
        val intent = Intent(context, TodoReminderReceiver::class.java).apply {
            putExtra("title", todo.title)
            putExtra("description", todo.description)
        }
        val requestCode = (todo.id.toInt() * 10) + requestCodeOffset
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (timeMillis > System.currentTimeMillis()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeMillis,
                pendingIntent
            )
        }
    }

    // 自定义提醒时间（如果有）
    todo.remindTime?.let {
        setAlarm(it, 1)
    }
    // 截止时间提醒
    setAlarm(todo.dueDay, 2)
}

fun cancelReminder(context: Context, todo: Todo) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun cancel(requestCodeOffset: Int) {
        val intent = Intent(context, TodoReminderReceiver::class.java)
        val requestCode = (todo.id.toInt() * 10) + requestCodeOffset
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    cancel(1) // 取消自定义提醒
    cancel(2) // 取消截止提醒
}
