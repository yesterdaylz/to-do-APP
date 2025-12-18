package com.example.todoapp.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.todoapp.R
import com.example.todoapp.data.entity.Todo
import com.example.todoapp.ui.viewmodel.TodoViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTodoDialogFragment: DialogFragment() {
    //获取共享的 ViewModel
    private val viewModel: TodoViewModel by activityViewModels()

    private var editingTodo: Todo? = null
    private var dueTimeMillis: Long = System.currentTimeMillis()
    private var remindTimeMillis: Long? = null
    private val dueCalendar: Calendar = Calendar.getInstance()
    private val remindCalendar: Calendar = Calendar.getInstance()
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editingTodo = arguments?.getParcelable(ARG_TODO)
        username = arguments?.getString(ARG_USERNAME) ?: ""
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater
            .inflate(R.layout.dialog_add, null)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val btnPickDueTime = view.findViewById<Button>(R.id.btnPickDueTime)
        val btnPickRemindTime = view.findViewById<Button>(R.id.btnPickRemindTime)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val spCategory = view.findViewById<android.widget.Spinner>(R.id.spCategory)
        val cbPin= view.findViewById<android.widget.CheckBox>(R.id.cbTop)
        val categories = listOf("默认", "学习", "工作", "生活", "其他")
        val adapterCategory = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spCategory.adapter = adapterCategory
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        editingTodo?.let { todo ->


            etTitle.setText(todo.title)
            etDescription.setText(todo.description ?: "")
            dueTimeMillis = todo.dueDay
            dueCalendar.timeInMillis = dueTimeMillis//
            remindTimeMillis = todo.remindTime
            remindTimeMillis?.let { remindCalendar.timeInMillis = it }
            val index = categories.indexOf(todo.category).takeIf { it >= 0 } ?: 0
            spCategory.setSelection(index)//设置选中索引
            cbPin.isChecked = todo.pin
            btnPickDueTime.text = "截止时间：${format.format(Date(todo.dueDay))}"
            todo.remindTime?.let {
                btnPickRemindTime.text = "提醒时间：${format.format(Date(it))}"
            }
        }





        btnPickDueTime.setOnClickListener {
            showDateTimePicker(
                initialCalendar = dueCalendar,
                onTimeSelected = { timeMillis, formatted ->
                    dueTimeMillis = timeMillis
                    btnPickDueTime.text = "截止时间：$formatted"
                }
            )
        }

        btnPickRemindTime.setOnClickListener {
            showDateTimePicker(
                initialCalendar = remindCalendar,
                onTimeSelected = { timeMillis, formatted ->
                    remindTimeMillis = timeMillis
                    btnPickRemindTime.text = "提醒时间：$formatted"
                }
            )
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDescription.text.toString().trim()
            val category = spCategory.selectedItem?.toString() ?: "默认"//NullPointerException哭了
            val pin = cbPin.isChecked

            if (title.isEmpty()) {
                etTitle.error = "标题不能为空"
                return@setOnClickListener
            }

            val todo = editingTodo?.copy(
                title = title,
                description = desc,
                dueDay = dueTimeMillis,
                remindTime = remindTimeMillis,
                category = category,
                pin = pin
            ) ?: run{
                val randomBg = (0 until TODO_BG_COUNT).random()
                Todo(
                    username = username,
                    title = title,
                    description = desc,
                    dueDay = dueTimeMillis,
                    remindTime = remindTimeMillis,
                    background = randomBg,
                    category = category,
                    pin = pin,
                    done = false
                )
            }


            if (editingTodo == null) {
                viewModel.addTodo(todo)
            } else {
                viewModel.updateTodo(todo)
            }

            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    companion object {
        private const val ARG_TODO = "arg_todo"
        private const val ARG_USERNAME = "arg_username"
        private const val  TODO_BG_COUNT = 10
        fun newInstance(todo: Todo?,username: String): AddTodoDialogFragment {
            val f = AddTodoDialogFragment()
            val args = Bundle()

            args.putParcelable(ARG_TODO, todo)
            args.putString(ARG_USERNAME, username)
            f.arguments = args
            return f
        }
    }
    private fun showDateTimePicker(
        initialCalendar: Calendar,
        onTimeSelected: (Long, String) -> Unit
    ) {

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("选择日期")
            .setSelection(initialCalendar.timeInMillis)
            .setTheme(R.style.MyDatePickerTheme)
            .build()

        datePicker.addOnPositiveButtonClickListener { utcMidnightMillis ->
            val pickedDateCal = Calendar.getInstance().apply { timeInMillis = utcMidnightMillis }
            initialCalendar.set(Calendar.YEAR, pickedDateCal.get(Calendar.YEAR))
            initialCalendar.set(Calendar.MONTH, pickedDateCal.get(Calendar.MONTH))
            initialCalendar.set(Calendar.DAY_OF_MONTH, pickedDateCal.get(Calendar.DAY_OF_MONTH))


            val timePicker = MaterialTimePicker.Builder()
                .setTitleText("选择时间")
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(initialCalendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(initialCalendar.get(Calendar.MINUTE))
                .setTheme(R.style.MyTimePickerTheme)
                .build()

            timePicker.addOnPositiveButtonClickListener {
                initialCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                initialCalendar.set(Calendar.MINUTE, timePicker.minute)
                initialCalendar.set(Calendar.SECOND, 0)
                initialCalendar.set(Calendar.MILLISECOND, 0)

                val timeMillis = initialCalendar.timeInMillis
                val formatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(Date(timeMillis))

                onTimeSelected(timeMillis, formatted)
            }

            timePicker.show(childFragmentManager, "TIME_PICKER")
        }

        datePicker.show(childFragmentManager, "DATE_PICKER")
    }



}