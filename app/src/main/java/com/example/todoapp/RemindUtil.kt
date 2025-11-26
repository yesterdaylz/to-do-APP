package com.example.todoapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.example.todoapp.data.entity.Todo
import com.example.todoapp.logic.TodoReminderReceiver

fun scheduleReminder(context: Context, todo: Todo) {
    //  用 applicationContext，避免和 Activity 生命周期绑死
    val appContext = context.applicationContext
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    // 检查通知权限
    if (!notificationsEnabled) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:${context.packageName}".toUri()
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(intent)
        return

    }
    // 检查是否允许 exact alarm
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
//            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
//                data = Uri.parse("package:${context.packageName}")
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = "package:${context.packageName}".toUri() // 使用 toUri() 替代 Uri.parse()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
            return
        }

    }

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = context.packageName

        // 检查是否忽略电池优化
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
        }

    fun setAlarm(timeMillis: Long, requestCodeOffset: Int) {
        if (timeMillis <= System.currentTimeMillis()) return

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            val info = AlarmManager.AlarmClockInfo(timeMillis, pendingIntent)
            alarmManager.setAlarmClock(info, pendingIntent)
        } else {
            alarmManager.setExact(
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
