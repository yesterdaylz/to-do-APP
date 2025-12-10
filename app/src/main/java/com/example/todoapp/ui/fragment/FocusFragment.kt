package com.example.todoapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentFocusBinding
import com.example.todoapp.logic.timer.TimerConfig
import com.example.todoapp.logic.timer.TimerMode
import com.example.todoapp.ui.activity.FocusTimerActivity


private const val ARG_USERNAME ="arg_username"
class FocusFragment : Fragment() {
    private var _binding: FragmentFocusBinding? = null
    private var username:String = ""
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            username = it.getString(ARG_USERNAME)?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFocusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPickers()
        setupMode()
        binding.btnStartFocus.setOnClickListener {
            startFocus()
        }
    }
    private fun setupPickers(){
        binding.pickerMinutes.apply {
            minValue = 1
            maxValue = 300
            value = 25///，默认值
        }
        binding.pickerPomodoroCount.apply {
            minValue = 1
            maxValue = 12
            value = 3
        }
        binding.pickerShortBreak.apply {
            minValue = 1
            maxValue = 60
            value = 5
        }
        binding.layoutPomodoroSet.visibility = View.GONE
    }
    private fun setupMode(){
        binding.rgMode.setOnCheckedChangeListener { _, checkedId ->
            binding.layoutPomodoroSet.visibility = if (checkedId == R.id.rbPomodoro) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        binding.rbStopwatch.isChecked = true
    }
    private fun startFocus(){
        val mode = when (binding.rgMode.checkedRadioButtonId) {
            R.id.rbCountdown -> TimerMode.COUNTDOWN
            R.id.rbPomodoro -> TimerMode.POMODORO
            else -> TimerMode.STOPWATCH
        }

        val minutes = binding.pickerMinutes.value
        val pomodoroCount = binding.pickerPomodoroCount.value
        val shortBreak = binding.pickerShortBreak.value

        val config = TimerConfig(
            mode = mode,
            minutes = minutes,
            pomodoroCount = pomodoroCount,
            shortBreakMinutes = shortBreak,
            longBreakMinutes = 15
        )

        val intent = Intent(requireContext(), FocusTimerActivity::class.java).apply {
            putExtra("config", config)
            putExtra("username", username)
        }
        startActivity(intent)
    }
    companion object {
        @JvmStatic
        fun newInstance(username: String) =
            FocusFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USERNAME, username)
                }
            }
    }
}