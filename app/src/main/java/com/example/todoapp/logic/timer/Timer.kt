package com.example.todoapp.logic.timer

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface Timer {
    val state: StateFlow<TimerState>
    val events: SharedFlow<TimerEvent>

    fun startOrResume()
    fun pause()
    fun stop()
}