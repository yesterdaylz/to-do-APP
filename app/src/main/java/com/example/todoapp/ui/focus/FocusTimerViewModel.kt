package com.example.todoapp.ui.focus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.todoapp.R
import com.example.todoapp.Repository
import com.example.todoapp.data.entity.TimeRecord
import com.example.todoapp.logic.motto.Quote
import com.example.todoapp.logic.timer.TimerConfig
import com.example.todoapp.logic.timer.TimerEvent
import com.example.todoapp.logic.timer.TimerLogic
import com.example.todoapp.logic.timer.TimerMode
import com.example.todoapp.logic.timer.TimerPhase
import com.example.todoapp.logic.timer.TimerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Locale

class FocusTimerViewModel(
    application: Application,
    private val config: TimerConfig,
    private val username: String,
    private val repository: Repository
) : AndroidViewModel(application) {
    private val timer = TimerLogic(viewModelScope, config)


    private val _quoteLiveData = MutableLiveData<String>()
    val quoteLiveData: LiveData<String> = _quoteLiveData
    private val quote = Quote()
    private var quoteJob: Job? = null
    private val _timeLiveData = MutableLiveData<String>()
    val timeLiveData: LiveData<String> = _timeLiveData
    private val _pomodoroInfoLiveData = MutableLiveData<String>()
    val pomodoroInfoLiveData: LiveData<String> = _pomodoroInfoLiveData
    private val _isFocusLiveData = MutableLiveData(false)
    //val isFocusLiveData: LiveData<Boolean> = _isFocusLiveData

    private val _timerFinished = MutableLiveData<Unit>()
    val timerFinishedEvent: LiveData<Unit> = _timerFinished
    // 是否开启背景音乐（默认开启）
    private val _isBgmEnabledLiveData = MutableLiveData(true)
    val isBgmEnabledLiveData: LiveData<Boolean> = _isBgmEnabledLiveData
    private val _timerSoundState = MutableLiveData<String>("STOP")
    val timerSoundState: LiveData<String> = _timerSoundState

    // 状态
    var isRunning: Boolean = false
        private set
    var durationEnded: Boolean = false
        private set
    // 计时状态
    private var durationStartTime: Long = 0L
    init {
        observeTimer()
        render(timer.state.value)
    }
    private fun startQuoteLoop() {
        if (quoteJob != null) return
        quoteJob = viewModelScope.launch {
            quote.quoteLoop(10_000L)
                //上游切io
                .flowOn(Dispatchers.IO)
                .catch { _quoteLiveData.value = application.getString(R.string.quote_load_failed) }
                .collect { text ->
                    _quoteLiveData.value = text
                }
        }
    }
    private fun stopQuoteLoop() {
        quoteJob?.cancel()
        quoteJob = null
    }


    private fun saveDuration(durationMinutes: Int) {
        val endTime = System.currentTimeMillis()
        if (durationStartTime == 0L) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTimeRecord(
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
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)

    }

    fun setBgmEnabled(enabled: Boolean) {
        _isBgmEnabledLiveData.value = enabled

        if (enabled) {
            if (isRunning) _timerSoundState.postValue("PLAY")
        } else {
            _timerSoundState.postValue("STOP")
        }
    }
    private fun observeTimer() {
        viewModelScope.launch {
            timer.state.collectLatest { st ->
                isRunning = st.isRunning
                durationEnded = st.durationEnded
                render(st)
            }
        }
        viewModelScope.launch {
            timer.events.collectLatest { ev ->
                if (ev is TimerEvent.Finished) {
                    _timerFinished.value = Unit
                    saveDuration(durationMinutesForSaving())
                    _timerSoundState.postValue("STOP")
                    stopQuoteLoop()
                }
            }
        }
    }
    private fun render(st: TimerState) {
        _timeLiveData.value = formatTime(st.seconds)

        if (config.mode == TimerMode.POMODORO) {
            val phaseText = if (st.phase == TimerPhase.BREAK)
                application.getString(R.string.break_phase)
            else
                application.getString(R.string.focus_phase)

            _pomodoroInfoLiveData.value = application.getString(
                R.string.pomodoro_info,
                st.currentPomodoro,
                config.pomodoroCount,
                phaseText
            )

            _isFocusLiveData.value = (st.phase == TimerPhase.FOCUS && st.isRunning)
        } else {
            _pomodoroInfoLiveData.value =
                if (config.mode == TimerMode.STOPWATCH)
                    application.getString(R.string.timer_target_duration, config.minutes)
                else ""

            _isFocusLiveData.value = st.isRunning
        }

        // BGM 控制（保持你的逻辑）
        if (_isBgmEnabledLiveData.value == true && st.isRunning && _isFocusLiveData.value == true) {
            _timerSoundState.postValue("PLAY")
        } else {
            _timerSoundState.postValue("STOP")
        }
    }
    fun startTimerOrResume() {
        if (durationStartTime == 0L) durationStartTime = System.currentTimeMillis()
        startQuoteLoop()
        timer.startOrResume()
    }

    fun pauseTimer() {
        timer.pause()
        stopQuoteLoop()
        _timerSoundState.postValue("STOP")
    }
    private fun durationMinutesForSaving(): Int {
        return if (config.mode == TimerMode.POMODORO) config.minutes * config.pomodoroCount else config.minutes
    }


    override fun onCleared() {
        super.onCleared()
        stopQuoteLoop()
        quoteJob?.cancel()
    }
}