package com.example.todoapp.ui.focus

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.Repository
import com.example.todoapp.logic.timer.TimerConfig

class FocusTimerViewModelFactory(
    private val application: Application,
    private val config: TimerConfig,
    private val username: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    //类型兼容性检查
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FocusTimerViewModel::class.java)) {
            val repo = Repository.getInstance(application)
            return FocusTimerViewModel(application, config, username, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}