package com.example.todoapp.ui.activity

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.R
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.databinding.ActivityFocusTimerBinding
import com.example.todoapp.logic.timer.TimerConfig
import com.example.todoapp.ui.viewmodel.FocusTimerViewModel
import com.example.todoapp.ui.viewmodel.FocusTimerViewModelFactory

class FocusTimerActivity : AppCompatActivity() {
    private lateinit var config: TimerConfig
    private lateinit var username: String
    private lateinit var viewModel: FocusTimerViewModel
    private lateinit var binding: ActivityFocusTimerBinding
    // 背景BGM（循环）
    private var bgmPlayer: MediaPlayer? = null
    // 完成提示音播放器（短音效，每次播完释放）
    private var finishPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFocusTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 初始化 ViewModel，不懂太新太旧都给我警告
        @SuppressLint("NewApi")
         config = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("config", TimerConfig::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("config") as? TimerConfig
        }!!
         username = intent.getStringExtra("username") ?: ""
        val db = TodoDatabase.getInstance(this)
        val factory = FocusTimerViewModelFactory(config, username, db)
        viewModel = ViewModelProvider(this, factory)[FocusTimerViewModel::class.java]
        observeViewModel()
        setupViews()
        viewModel.startQuoteLoop()
        setupBackPressed()
    }
    private fun observeViewModel() {
        viewModel.timeLiveData.observe(this) { time ->
            binding.tvTime.text = time
        }
        viewModel.pomodoroInfoLiveData.observe(this) { info ->
            binding.tvPomodoroInfo.text = info
        }
        viewModel.quoteLiveData.observe(this) { quote ->
            binding.tvDailyQuote.text = quote
        }
        viewModel.isFocusRunningLiveData.observe(this) { isRunning ->
            val bgmEnabled = viewModel.isBgmEnabledLiveData.value ?: true
            if (isRunning && bgmEnabled) {
                startBgm()
            } else {
                stopBgm()
            }
        }
        viewModel.timerFinishedEvent.observe(this) {
            stopBgm()          // 确保 BGM 停掉
            playFinishSound()  // 提示音照常播放
        }
        viewModel.isBgmEnabledLiveData.observe(this) { enabled ->
            // 更新 RadioButton UI（防止旋转屏幕后状态错乱）
            binding.rbMusicToggle.isChecked = enabled

            if (!enabled) {
                // 关掉音乐：立刻停掉 BGM
                stopBgm()
            } else {
                // 如果当前处于专注计时中，则恢复播放
                val isFocus = viewModel.isFocusRunningLiveData.value ?: false
                if (isFocus) {
                    startBgm()
                }
            }
        }

    }

    private fun setupViews() {
        binding.btnStartPause.setOnClickListener {
            if (viewModel.isRunning) pauseTimer() else startTimer()
        }
        binding.btnFinish.setOnClickListener {
            confirmExit()
        }
        binding.rbMusicToggle.isChecked = true   // 默认开启
        binding.rbMusicToggle.setOnCheckedChangeListener { _, isChecked ->
            // 更新 ViewModel 状态
            viewModel.setBgmEnabled(isChecked)
        }
    }
    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    confirmExit()
                }
            })
    }

    private fun startTimer() {
        viewModel.startTimerOrResume()
        binding.btnStartPause.text = "暂停"
    }
    private fun pauseTimer() {
        viewModel.pauseTimer()
        binding.btnStartPause.text = "继续"
    }
    private fun confirmExit() {
        if (viewModel.durationEnded || !viewModel.isRunning) {
            finish()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage("专注计时还未结束，确定要退出吗？")
            .setPositiveButton("退出") { _, _ -> finish() }
            .setNegativeButton("继续专注", null)
            .show()
    }
    private fun startBgm() {
        if (bgmPlayer == null) {
            bgmPlayer = MediaPlayer.create(this, R.raw.focus_bgm).apply {
                isLooping = true
            }
        }
        if (bgmPlayer?.isPlaying != true) {
            bgmPlayer?.start()
        }
    }

    private fun stopBgm() {
        bgmPlayer?.pause()
        // bgmPlayer?.seekTo(0)
    }
    private fun playFinishSound() {
        // 上一次的先释放
        finishPlayer?.release()
        finishPlayer = MediaPlayer.create(this, R.raw.timer_finish)
        finishPlayer?.setOnCompletionListener { mp ->
            mp.release()
            if (finishPlayer == mp) {
                finishPlayer = null
            }
        }
        finishPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放资源
        bgmPlayer?.release()
        bgmPlayer = null
        finishPlayer?.release()
        finishPlayer = null
    }
}

