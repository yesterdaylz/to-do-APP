package com.example.todoapp.logic.timer

enum class TimerPhase { FOCUS, BREAK }
data class TimerState(
    val mode: TimerMode,
    val isRunning: Boolean = false,
    val durationEnded: Boolean = false,
    val phase: TimerPhase = TimerPhase.FOCUS,
    val currentPomodoro: Int = 1,
    val pomodoroCount: Int = 1,
    val seconds: Int = 0
)
sealed class TimerEvent {
    data object Finished : TimerEvent()
}