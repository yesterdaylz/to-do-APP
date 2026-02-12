package com.example.todoapp.ui.record

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentRecordBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class RecordFragment : Fragment() {
    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecordViewmodel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        setupChart()
        observeViewModel()
        return binding.root
    }
    //配置表格
    private fun setupChart() {
        //判断黑夜模式，设置图表文字颜色
        val isDarkMode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val textColor = if (isDarkMode) {
            Color.WHITE
        } else {
            Color.BLACK
        }
        with(binding.barChart) {
            description.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.setDrawAxisLine(true)
            axisLeft.setDrawGridLines(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisLeft.axisMinimum = 0f
            legend.isEnabled = true
            setScaleEnabled(false)
            setFitBars(true)
            xAxis.textColor = textColor
            axisLeft.textColor = textColor
            legend.textColor = textColor
            invalidate()
        }
    }

    private fun observeViewModel() {
        // 收集今日统计状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.todayInfo.collectLatest { state ->
                binding.tvTodayInfo.text = getString(
                    R.string.record_today_info,
                    state.count,
                    state.minutes
                )
            }
        }

        // 收集图表数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chartData.collectLatest { state ->
                val entries = state.values.mapIndexed { index, value ->
                    BarEntry(index.toFloat(), value)
                }
                val dataSet = BarDataSet(
                    entries,
                    getString(state.labelText)
                )
                dataSet.color = "#4CAF50".toColorInt()
                val barData = BarData(dataSet)
                barData.barWidth = 0.6f
                binding.barChart.data = barData
                binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(state.labels)
                binding.barChart.xAxis.labelCount = state.labels.size
                binding.barChart.animateXY(1000, 1000)
                binding.barChart.invalidate()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}