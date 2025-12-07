package com.example.todoapp.logic.timer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class TimerConfig(
    val mode: TimerMode,
    val minutes: Int,
    val pomodoroCount: Int = 1,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15
) : Parcelable
