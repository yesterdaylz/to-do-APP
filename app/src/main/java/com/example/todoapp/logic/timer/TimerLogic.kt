package com.example.todoapp.logic.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerLogic (
    private val scope: CoroutineScope,
    private val config: TimerConfig
    ): Timer{

    private val _state = MutableStateFlow(
        TimerState(
            mode = config.mode,
            isRunning = false,
            durationEnded = false,
            phase = TimerPhase.FOCUS,
            currentPomodoro = 1,
            pomodoroCount = if (config.mode == TimerMode.POMODORO) config.pomodoroCount else 1,
            seconds = initialSeconds()
        )
    )
    override val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    private var job: Job? = null

    // 缓存用于暂停/恢复
    private var cachedSeconds: Int = _state.value.seconds
    private var phase: TimerPhase = TimerPhase.FOCUS
    private var currentPomodoro: Int = 1

    override fun startOrResume() {
        if (job != null) return

        // 同步缓存（防止外部 stop 后不一致）
        cachedSeconds = _state.value.seconds
        phase = _state.value.phase
        currentPomodoro = _state.value.currentPomodoro

        _state.value = _state.value.copy(isRunning = true, durationEnded = false)

        job = scope.launch {
            while (isActive) {
                delay(1000)
                tick()
            }
        }
    }

    override fun pause() {
        job?.cancel()
        job = null
        _state.value = _state.value.copy(isRunning = false)
    }

    override fun stop() {
        pause()
        phase = TimerPhase.FOCUS
        currentPomodoro = 1
        cachedSeconds = initialSeconds()
        _state.value = _state.value.copy(
            isRunning = false,
            durationEnded = false,
            phase = phase,
            currentPomodoro = currentPomodoro,
            seconds = cachedSeconds
        )
    }

    private fun tick() {
        when (config.mode) {
            TimerMode.STOPWATCH -> tickStopwatch()
            TimerMode.COUNTDOWN -> tickCountdown()
            TimerMode.POMODORO -> tickPomodoro()
        }
    }

    private fun tickStopwatch() {
        val target = config.minutes * 60
        cachedSeconds += 1
        if (cachedSeconds >= target) {
            cachedSeconds = target
            _state.value = _state.value.copy(seconds = cachedSeconds, isRunning = false, durationEnded = true)
            finish()
        } else {
            _state.value = _state.value.copy(seconds = cachedSeconds)
        }
    }

    private fun tickCountdown() {
        cachedSeconds -= 1
        if (cachedSeconds <= 0) {
            cachedSeconds = 0
            _state.value = _state.value.copy(seconds = 0, isRunning = false, durationEnded = true)
            finish()
        } else {
            _state.value = _state.value.copy(seconds = cachedSeconds)
        }
    }

    private fun tickPomodoro() {
        cachedSeconds -= 1
        if (cachedSeconds <= 0) {
            cachedSeconds = 0
            _state.value = _state.value.copy(seconds = 0)
            onPomodoroPhaseFinished()
        } else {
            _state.value = _state.value.copy(seconds = cachedSeconds)
        }
    }

    private fun onPomodoroPhaseFinished() {
        if (phase == TimerPhase.FOCUS) {
            if (currentPomodoro >= config.pomodoroCount) {
                _state.value = _state.value.copy(isRunning = false, durationEnded = true)
                finish()
                return
            }
            // 进入休息
            phase = TimerPhase.BREAK
            cachedSeconds = breakSeconds()
        } else {
            // 休息结束 -> 下一轮专注
            phase = TimerPhase.FOCUS
            currentPomodoro += 1
            cachedSeconds = config.minutes * 60
        }

        _state.value = _state.value.copy(
            phase = phase,
            currentPomodoro = currentPomodoro,
            seconds = cachedSeconds,
            isRunning = true,
            durationEnded = false
        )
    }

    private fun breakSeconds(): Int {
        val isLongBreak = (currentPomodoro % 4 == 0)
        val breakMin = if (isLongBreak) config.longBreakMinutes else config.shortBreakMinutes
        return breakMin * 60
    }

    private fun finish() {
        pause()
        _events.tryEmit(TimerEvent.Finished)
    }

    private fun initialSeconds(): Int {
        return when (config.mode) {
            TimerMode.STOPWATCH -> 0
            TimerMode.COUNTDOWN -> config.minutes * 60
            TimerMode.POMODORO -> config.minutes * 60
        }
    }
}