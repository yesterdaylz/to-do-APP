package com.example.todoapp.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.databinding.ActivityFocusTimerBinding
import com.example.todoapp.logic.timer.TimerConfig
import com.example.todoapp.ui.viewmodel.FocusTimerViewModel
import com.example.todoapp.ui.viewmodel.FocusTimerViewModelFactory
class FocusTimerActivity : AppCompatActivity() {
    private lateinit var config: TimerConfig
    private lateinit var username: String
    private val viewModel: FocusTimerViewModel by viewModels {
        FocusTimerViewModelFactory(config, username, TodoDatabase.getInstance(this))
    }
    private lateinit var binding: ActivityFocusTimerBinding

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
        observeViewModel()
        setupViews()
        setupBackPressed()
    }
    private fun observeViewModel() {
        viewModel.timeLiveData.observe(this) { time ->
            binding.tvTime.text = time
        }
        viewModel.pomodoroInfoLiveData.observe(this) { info ->
            binding.tvPomodoroInfo.text = info
        }
    }

    private fun setupViews() {
        binding.btnStartPause.setOnClickListener {
            if (viewModel.isRunning) pauseTimer() else startTimer()
        }
        binding.btnFinish.setOnClickListener {
            confirmExit()
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
}

