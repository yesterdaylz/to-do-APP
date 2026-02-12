package com.example.todoapp.ui.record

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.R
import com.example.todoapp.Repository
import com.example.todoapp.data.entity.TimeRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class RecordViewmodel(app: Application) : AndroidViewModel(app) {

    private val repository = Repository.getInstance(app)

    private val prefs = app.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
    private val _todayInfo = MutableStateFlow(TodayInfoUiState())
    val todayInfo: StateFlow<TodayInfoUiState> = _todayInfo

    private val _chartData = MutableStateFlow(ChartUiState())
    val chartData: StateFlow<ChartUiState> = _chartData

    init {
        loadData()
    }

    //加载数据
    private fun loadData() {
        val username = prefs.getString("login_user", null) ?: return
        viewModelScope.launch {
            //监听所有时间记录的变化
            repository.getAllTimeRecords(username).collectLatest { list ->
                updateTodayData(list)
                updateChart(list)
            }
        }
    }
    /**
     * 更新今日数据
     * 计算得到今日开始和结束的时间戳
     * 持续时长00:00:00.000 - 23:59:59.999
     * 筛选出开始时间在此范围内的记录
     * 计算数量和总时长
     */

    private fun updateTodayData(list: List<TimeRecord>) {

        val today = Calendar.getInstance()
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
        _todayInfo.value = TodayInfoUiState(count = count, minutes = minutes)
    }

    /**
     * 更新图表数据
     * 计算得到七日前的开始时间戳
     * 7日循环，计算每一天的开始和结束时间戳
     * 筛选出开始时间在此范围内的记录
     * 计算每天的总时长，添加到列表中
     * 生成对应的日期标签
     */

    private fun updateChart(list: List<TimeRecord>) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dayMills = 24 * 60 * 60 * 1000L
        val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
        val labels = mutableListOf<String>()
        val values = mutableListOf<Float>()

        // 7 天前 -> 今天
        calendar.timeInMillis -= 6 * dayMills
        for (i in 0 until 7) {
            val dayStart = calendar.timeInMillis
            val dayEnd = dayStart + dayMills
            val dayDuration = list.filter {
                it.startTime in dayStart until dayEnd
            }
            val minutes = dayDuration.sumOf { it.duration }

            labels.add(dateFormat.format(Date(dayStart)))
            values.add(minutes.toFloat())

            calendar.timeInMillis += dayMills
        }

        _chartData.value = ChartUiState(
            labels = labels,
            values = values,
            labelText = R.string.chart_label_last_7_days
        )
    }
}