package com.example.todoapp.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.todoapp.R
import com.example.todoapp.data.entity.Todo
import com.example.todoapp.databinding.DialogAddBinding
import com.example.todoapp.ui.todo.TodoViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTodoDialogFragment: DialogFragment() {

    private val viewModel: TodoViewModel by activityViewModels()
    private var _binding: DialogAddBinding? = null
    private val binding get() = _binding!!
    private var editingTodo: Todo? = null
    private var dueTimeMillis: Long = System.currentTimeMillis()
    private var remindTimeMillis: Long? = null
    private val dueCalendar: Calendar = Calendar.getInstance()
    private val remindCalendar: Calendar = Calendar.getInstance()
    private lateinit var username: String
    private val prefs by lazy {
        requireContext().getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editingTodo = arguments?.getParcelable(ARG_TODO)
        //username = arguments?.getString(ARG_USERNAME) ?: ""
        username = prefs.getString("login_user",null) ?: ""
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddBinding.inflate(requireActivity().layoutInflater)
        val view = binding.root
        val categories = listOf(
            getString(R.string.category_default),
            getString(R.string.category_study),
            getString(R.string.category_work),
            getString(R.string.category_life),
            getString(R.string.category_other)
        )
        val adapterCategory = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spCategory.adapter = adapterCategory
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        editingTodo?.let { todo ->
            binding.etTitle.setText(todo.title)
            binding.etDescription.setText(todo.description ?: "")
            dueTimeMillis = todo.dueDay
            dueCalendar.timeInMillis = dueTimeMillis
            remindTimeMillis = todo.remindTime
            remindTimeMillis?.let { remindCalendar.timeInMillis = it }
            val  index = categories.indexOf(todo.category).coerceAtLeast(0)
            binding.spCategory.setSelection(index)//设置选中索引
            binding.cbPin.isChecked = todo.pin
            binding.btnPickDueTime.text = getString(R.string.due_time_prefix, format.format(Date(todo.dueDay)))
            todo.remindTime?.let {
                binding.btnPickRemindTime.text = getString(
                    R.string.remind_time_prefix, format.format(
                        Date(
                            it
                        )
                    ))
            }
        }


        binding.btnPickDueTime.setOnClickListener {
            showDateTimePicker(
                initialCalendar = dueCalendar,
                onTimeSelected = { timeMillis, formatted ->
                    dueTimeMillis = timeMillis
                    binding.btnPickDueTime.text = getString(R.string.due_time_prefix, formatted)
                }
            )
        }

        binding.btnPickRemindTime.setOnClickListener {
            showDateTimePicker(
                initialCalendar = remindCalendar,
                onTimeSelected = { timeMillis, formatted ->
                    remindTimeMillis = timeMillis
                    binding.btnPickRemindTime.text = getString(R.string.remind_time_prefix, formatted)
                }
            )
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val desc = binding.etDescription.text.toString().trim()
            val category = binding.spCategory.selectedItem?.toString() ?: "默认"
            val pin = binding.cbPin.isChecked
            if (title.isEmpty()) {
                binding.etTitle.error = getString(R.string.error_title_empty)
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
        //private const val ARG_USERNAME = "arg_username"
        private const val  TODO_BG_COUNT = 10
        fun newInstance(todo: Todo?): AddTodoDialogFragment {
            val f = AddTodoDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_TODO, todo)
            //args.putString(ARG_USERNAME, username)
            f.arguments = args
            return f
        }
    }
    private fun showDateTimePicker(
        initialCalendar: Calendar,
        onTimeSelected: (Long, String) -> Unit
    ) {

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.date_picker_title))
            .setSelection(initialCalendar.timeInMillis)
            .setTheme(R.style.MyDatePickerTheme)
            .build()

        datePicker.addOnPositiveButtonClickListener { utcMidnightMillis ->
            val pickedDateCal = Calendar.getInstance().apply { timeInMillis = utcMidnightMillis }
            initialCalendar.set(Calendar.YEAR, pickedDateCal.get(Calendar.YEAR))
            initialCalendar.set(Calendar.MONTH, pickedDateCal.get(Calendar.MONTH))
            initialCalendar.set(Calendar.DAY_OF_MONTH, pickedDateCal.get(Calendar.DAY_OF_MONTH))


            val timePicker = MaterialTimePicker.Builder()
                .setTitleText(getString(R.string.time_picker_title))
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