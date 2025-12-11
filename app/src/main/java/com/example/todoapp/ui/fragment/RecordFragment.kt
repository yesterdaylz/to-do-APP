package com.example.todoapp.ui.fragment

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.data.entity.TimeRecord
import com.example.todoapp.databinding.FragmentRecordBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val ARG_USERNAME = "arg_username"
class RecordFragment : Fragment() {
    private var _binding: FragmentRecordBinding? = null
    private var username:String = ""
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            username = it.getString(ARG_USERNAME, "")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        setupChart()
        loadData()
        return binding.root
    }
    //配置表格
    private fun setupChart() {
        val isDarkMode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        val textColor = if (isDarkMode) {
            Color.WHITE  // 深色模式下使用白色文字
        } else {
            Color.BLACK  // 浅色模式下使用黑色文字
        }
        with(binding.barChart) {
            description.isEnabled = false
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            //xAxis.granularity = 1f
            axisLeft.axisMinimum = 0f
            legend.isEnabled = true
            setScaleEnabled(false)  // 禁用缩放
            setFitBars(true)
            xAxis.setDrawGridLines(false) // 隐藏X轴网格线
            axisLeft.setDrawGridLines(true) // 显示Y轴网格线
            axisLeft.setDrawAxisLine(true)
            //setViewPortOffsets(80f, 80f, 10f, 50f)
            xAxis.textColor = textColor
            axisLeft.textColor = textColor
            legend.textColor = textColor
            invalidate()
        }
    }
    //加载数据
    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = withContext(Dispatchers.IO) {
                TodoDatabase.getInstance(requireContext())
            }
            db.timeRecordDAO().getAllRecord(username).collectLatest { list ->
                updateTodayData(list)
                updateChart(list)
            }
        }
    }

    private fun updateTodayData(list: List<TimeRecord>) {
        val today = Calendar.getInstance()
        //当天凌晨00:00:00.000的时间点
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        val startOfToday = today.timeInMillis
        val endOfToday = startOfToday + 24 * 60 * 60 * 1000L
        val todayDuration = list.filter {
            it.startTime in startOfToday until endOfToday
        }
        val count = todayDuration.size
        val minutes = todayDuration.sumOf { it.duration }
        binding.tvTodayInfo.text = "今天专注：$count 次，$minutes 分钟"
    }
    private fun updateChart(list: List<TimeRecord>) {
        // 最近 7 天
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dayMills = 24 * 60 * 60 * 1000L
        val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
        val labels = mutableListOf<String>()
        val entries = mutableListOf<BarEntry>()

        // 7天前 -> 今天
        calendar.timeInMillis -=  6 * dayMills
        for (i in 0 until 7) {
            val dayStart = calendar.timeInMillis
            val dayEnd = dayStart + dayMills
            val dayDuration = list.filter {
                it.startTime in dayStart until dayEnd
            }
            val minutes = dayDuration.sumOf { it.duration }
            labels.add(dateFormat.format(Date(dayStart)))
            entries.add(BarEntry(i.toFloat(), minutes.toFloat()))
            calendar.timeInMillis +=  dayMills
        }
        val dataSet = BarDataSet(entries, "最近7天专注时长(分钟)")
        dataSet.color = Color.parseColor("#4CAF50")
        val barData = BarData(dataSet)
        barData.barWidth = 0.6f
        binding.barChart.data = barData
        // x 轴标签
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.barChart.xAxis.labelCount = labels.size
        binding.barChart.animateXY(1000,1000)
        //binding.barChart.xAxis.setAvoidFirstLastClipping(true) // 避免首尾标签被裁剪
        //刷新图表显示
        binding.barChart.invalidate()
    }

    companion object {
        @JvmStatic
        fun newInstance(username: String) =
            RecordFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USERNAME, username)
                }
            }
    }
}