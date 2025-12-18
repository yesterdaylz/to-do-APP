package com.example.todoapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.logic.timer.TimerConfig
class FocusTimerViewModelFactory(
    private val application: Application,
    private val config: TimerConfig,
    private val username: String,
    private val database: TodoDatabase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    //类型兼容性检查
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FocusTimerViewModel::class.java)) {
            return FocusTimerViewModel(application,config, username, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
