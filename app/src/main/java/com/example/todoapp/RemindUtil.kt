package com.example.todoapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.example.todoapp.data.entity.Todo
import com.example.todoapp.logic.TodoReminderReceiver

fun scheduleReminder(context: Context, todo: Todo) {
    //  用 applicationContext，避免和Activity生命周期绑死
    val appContext = context.applicationContext
    //获取系统闹钟服务
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    //val powerManager= context.getSystemService(Context.POWER_SERVICE) as PowerManager
    //Android8.0要通知要单独授权
    val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    val packageName = context.packageName
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
        //独立任务栈
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(intent)
        return

    }
    // 检查是否允许 exact alarm
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {

            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = "package:${packageName}".toUri() // 使用 toUri() 替代 Uri.parse()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
            return
        }

    }
    // 违反了 Google Play 商店关于电池优化豁免的使用政策，仅允许在特定核心功能必需时申请😶
        // 检查是否忽略电池优化
//        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
//            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
//                //data = Uri.parse("package:$packageName")
//                data =  "package:${packageName}".toUri()
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//            appContext.startActivity(intent)
//        }

    fun setAlarm(timeMillis: Long, requestCodeOffset: Int,autoDone: Boolean) {
        if (timeMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, TodoReminderReceiver::class.java).apply {
            putExtra("todo_id", todo.id)
            putExtra("title", todo.title)
            putExtra("description", todo.description)
            putExtra("auto_done", autoDone)
        }
        //唯一请求码
        val requestCode = (todo.id.toInt() * 10) + requestCodeOffset
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        //记录、注册info
        val info = AlarmManager.AlarmClockInfo(timeMillis, pendingIntent)
        alarmManager.setAlarmClock(info, pendingIntent)

    }
    todo.remindTime?.let {
        setAlarm(it, 1,false)// 自定义提醒时间（如果有）
    }
    setAlarm(todo.dueDay, 2,true)// 截止时间提醒
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
