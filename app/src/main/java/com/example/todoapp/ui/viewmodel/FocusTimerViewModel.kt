package com.example.todoapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.data.entity.TimeRecord
import com.example.todoapp.logic.timer.TimerConfig
import com.example.todoapp.logic.timer.TimerMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FocusTimerViewModel(
    private val config: TimerConfig,
    private val username: String,
    private val database: TodoDatabase
) : ViewModel() {
    // LiveData
    private val _timeLiveData = MutableLiveData<String>()
    val timeLiveData: LiveData<String> = _timeLiveData
    private val _pomodoroInfoLiveData = MutableLiveData<String>()
    val pomodoroInfoLiveData: LiveData<String> = _pomodoroInfoLiveData
    // 状态
    var isRunning: Boolean = false
        private set
    var durationEnded: Boolean = false
        private set
    // 计时状态
    private var durationStartTime: Long = 0L
    private var stopwatchJob: Job? = null
    private var countdownJob: Job? = null
    private var breakJob: Job? = null
    private var inBreak: Boolean = false
    private var currentPomodoro: Int = 1
    private var totalWorkMinutes: Int = 0
    init {
        updateUi()
    }
    fun startTimerOrResume() {
        if (durationStartTime == 0L) {
            durationStartTime = System.currentTimeMillis()
        }
        when (config.mode) {
            TimerMode.STOPWATCH -> startStopwatch()
            TimerMode.COUNTDOWN -> startCountdown(config.minutes * 60L)
            TimerMode.POMODORO -> {
                if (inBreak) {
                    startBreakTimer()
                } else {
                    startCountdown(config.minutes * 60L)
                }
            }
        }
        isRunning = true
    }
    fun pauseTimer() {
        stopwatchJob?.cancel()
        countdownJob?.cancel()
        breakJob?.cancel()
        isRunning = false
    }
    private fun updateUi() {
        when (config.mode) {
            TimerMode.STOPWATCH -> {
                _timeLiveData.value = formatTime(0)
                _pomodoroInfoLiveData.value = "目标时长：${config.minutes} 分钟"
            }
            TimerMode.COUNTDOWN -> {
                _timeLiveData.value = formatTime(config.minutes * 60)
                _pomodoroInfoLiveData.value = ""
            }
            TimerMode.POMODORO -> {
                _timeLiveData.value = formatTime(config.minutes * 60)
                updatePomodoroInfo()
            }
        }
    }

    private fun updatePomodoroInfo() {
        if (config.mode != TimerMode.POMODORO) return
        val phase = if (inBreak) "休息" else "专注"
        _pomodoroInfoLiveData.value =
            "第 $currentPomodoro/${config.pomodoroCount} 个番茄 - 当前：$phase"
    }
    //正计时
    private fun startStopwatch() {
        stopwatchJob?.cancel()
        stopwatchJob = viewModelScope.launch {
            var passedSeconds = 0
            val targetSeconds = config.minutes * 60
            while (passedSeconds < targetSeconds) {
                delay(1000)
                passedSeconds++
                _timeLiveData.value = formatTime(passedSeconds)
            }
            // 完成一次专注
            isRunning = false
            durationEnded = true
            saveDuration(config.minutes)
        }
    }
    //倒计时/番茄钟
    private fun startCountdown(totalSeconds: Long) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remainingSeconds = totalSeconds
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
                _timeLiveData.value = formatTime(remainingSeconds.toInt())
            }
            _timeLiveData.value = formatTime(0)

            when (config.mode) {
                TimerMode.COUNTDOWN -> {
                    isRunning = false
                    durationEnded = true
                    saveDuration(config.minutes)
                }
                TimerMode.POMODORO -> onPomodoroFinished()
                else -> {}
            }
        }
    }

    private fun onPomodoroFinished() {
        totalWorkMinutes += config.minutes
        if (currentPomodoro >= config.pomodoroCount) {
            // 所有番茄完成
            isRunning = false
            durationEnded = true
            saveDuration(totalWorkMinutes)
        } else {
            // 进入休息
            inBreak = true
            updatePomodoroInfo()
            val isLongBreak = (currentPomodoro % 4 == 0)
            val breakMinutes =
                if (isLongBreak) config.longBreakMinutes else config.shortBreakMinutes
            startBreakTimer(breakMinutes)
        }
    }
    private fun startBreakTimer(breakMinutes: Int = config.shortBreakMinutes) {
        breakJob?.cancel()
        breakJob = viewModelScope.launch {
            var remainingSeconds = breakMinutes * 60
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
                _timeLiveData.value = "休息 " + formatTime(remainingSeconds)
            }
            // 休息结束，下一轮工作
            inBreak = false
            currentPomodoro++
            updatePomodoroInfo()
            startCountdown(config.minutes * 60L)
        }
        isRunning = true
    }
    private fun saveDuration(durationMinutes: Int) {
        val endTime = System.currentTimeMillis()
        if (durationStartTime == 0L) return

        viewModelScope.launch(Dispatchers.IO) {
            database.timeRecordDAO().insert(
                TimeRecord(
                    username = username,
                    mode = config.mode.name,
                    startTime = durationStartTime,
                    endTime = endTime,
                    duration = durationMinutes,
                    tomatoCount = if (config.mode == TimerMode.POMODORO)
                        config.pomodoroCount
                    else
                        1
                )
            )
        }
    }
    //转换成电子钟格式
    private fun formatTime(totalSeconds: Int): String {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return String.format("%02d:%02d", m, s)
    }

    override fun onCleared() {
        super.onCleared()
        stopwatchJob?.cancel()
        countdownJob?.cancel()
        breakJob?.cancel()
    }
}