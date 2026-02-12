package com.example.todoapp.ui.record

import com.example.todoapp.R

data class TodayInfoUiState(
    val count: Int = 0,
    val minutes: Int = 0
)

data class ChartUiState(
    val labels: List<String> = emptyList(),//X轴的标签
    val values: List<Float> = emptyList(),//柱子数值
    val labelText: Int = R.string.chart_label_last_7_days//图表说明文字
)