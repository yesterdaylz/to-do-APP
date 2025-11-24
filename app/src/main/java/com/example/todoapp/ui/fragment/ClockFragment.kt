package com.example.todoapp.ui.fragment

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.todoapp.databinding.FragmentClockBinding


class ClockFragment : Fragment() {
    var running = false
    var offset:Long = 0
    val OFFSET_KEY = "offset"
    val RUNNING_KEY = "running"
    val BASE_KEY = "base"
    private lateinit var binding: FragmentClockBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentClockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.start.setOnClickListener {
            if (!running) {
                setBaseTime()
                binding.watcher.start()
                running = true
            }
        }
        binding.stop.setOnClickListener {
            if (running) {
                saveOffset()
                binding.watcher.stop()
                running = false
            }
        }
        binding.reset.setOnClickListener {
            offset = 0
            setBaseTime()
        }
    }
    fun setBaseTime(){
        //stopwatch.base = SystemClock.elapsedRealtime() - offset
       binding.watcher.setBase(SystemClock.elapsedRealtime() - offset)

    }
    fun saveOffset(){
        //offset = System.currentTimeMillis() - stopwatch.base
        offset = SystemClock.elapsedRealtime() - binding.watcher.base

    }




}