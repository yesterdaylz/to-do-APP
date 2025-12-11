package com.example.todoapp.ui.activity

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.CompoundButton
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

    private lateinit var viewModel: FocusTimerViewModel
    private lateinit var binding: ActivityFocusTimerBinding

    // 背景BGM（循环）
    private var bgmPlayer: MediaPlayer? = null

    // 完成提示音播放器（短音效，每次播完释放）
    private var finishPlayer: MediaPlayer? = null
    private var musicToggleListener: CompoundButton.OnCheckedChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFocusTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 初始化 ViewModel，不懂太新太旧都给我警告
        @SuppressLint("NewApi")
        val config =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("config", TimerConfig::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("config") as? TimerConfig
            }!!
        val username = intent.getStringExtra("username") ?: ""
        val db = TodoDatabase.getInstance(this)
        val factory = FocusTimerViewModelFactory(config, username, db)
        viewModel = ViewModelProvider(this, factory)[FocusTimerViewModel::class.java]
        observeViewModel()
        setupViews()
        viewModel.startQuoteLoop()
        setupBackPressed()
    }

    private fun observeViewModel() {
        viewModel.timeLiveData.observe(this) {
            binding.tvTime.text = it
        }
        viewModel.pomodoroInfoLiveData.observe(this) {
            binding.tvPomodoroInfo.text = it
        }
        viewModel.quoteLiveData.observe(this) {
            binding.tvDailyQuote.text = it
        }
//        viewModel.isFocusRunningLiveData.observe(this) { isRunning ->
//            val bgmEnabled = viewModel.isBgmEnabledLiveData.value ?: true
//            if (bgmEnabled) {
//                if (isRunning) startBgm() else stopBgm()
//            } else {
//                stopBgm()
//            }
//        }
        viewModel.timerSoundState.observe(this) { state ->
            when (state) {
                "PLAY" -> startBgm()
                "STOP" -> stopBgm()
            }
        }
        viewModel.timerFinishedEvent.observe(this) {
            stopBgm()          // 确保 BGM 停掉
            playFinishSound()  // 提示音照常播放
        }
        viewModel.isBgmEnabledLiveData.observe(this) { enabled ->
            if (binding.cbMusicToggle.isChecked != enabled) {
                binding.cbMusicToggle.setOnCheckedChangeListener(null)
                binding.cbMusicToggle.isChecked = enabled
                binding.cbMusicToggle.setOnCheckedChangeListener(musicToggleListener)
            }
        }


        viewModel.timerFinishedEvent.observe(this) {
            stopBgm()
            playFinishSound()
        }
    }


    private fun setupViews() {
        binding.btnStartPause.setOnClickListener {
            if (viewModel.isRunning) pauseTimer() else startTimer()
        }

        binding.btnFinish.setOnClickListener {
            confirmExit()
        }

        musicToggleListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            viewModel.setBgmEnabled(isChecked)
        }

        binding.cbMusicToggle.setOnCheckedChangeListener(musicToggleListener)
    }


    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(
            this,
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
        if (viewModel.isBgmEnabledLiveData.value != true) return

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

