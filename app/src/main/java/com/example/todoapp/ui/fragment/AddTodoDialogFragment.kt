package com.example.todoapp.ui.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.todoapp.R
import com.example.todoapp.data.entity.Todo
import com.example.todoapp.ui.viewmodel.TodoViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTodoDialogFragment: DialogFragment() {
    private val viewModel: TodoViewModel by activityViewModels()

    private var editingTodo: Todo? = null
    private var dueTimeMillis: Long = System.currentTimeMillis()
    private var remindTimeMillis: Long? = null
    private val dueCalendar: Calendar = Calendar.getInstance()
    private val remindCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editingTodo = arguments?.getParcelable(ARG_TODO)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater
            .inflate(R.layout.dialog_add, null)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val btnPickDueTime = view.findViewById<Button>(R.id.btnPickDueTime)
        val btnPickRemindTime = view.findViewById<Button>(R.id.btnPickRemindTime)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        editingTodo?.let { todo ->
            etTitle.setText(todo.title)
            etDescription.setText(todo.description ?: "")
            dueTimeMillis = todo.dueDay
            remindTimeMillis = todo.remindTime
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

            if (title.isEmpty()) {
                etTitle.error = "标题不能为空"
                return@setOnClickListener
            }

            val todo = editingTodo?.copy(
                title = title,
                description = desc,
                dueDay = dueTimeMillis,
                remindTime = remindTimeMillis
            ) ?: Todo(
                title = title,
                description = desc,
                dueDay = dueTimeMillis,
                remindTime = remindTimeMillis
            )

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

        fun newInstance(todo: Todo?): AddTodoDialogFragment {
            val f = AddTodoDialogFragment()
            val args = Bundle()

            args.putParcelable(ARG_TODO, todo)
            f.arguments = args
            return f
        }
    }
    private fun showDateTimePicker(
        initialCalendar: Calendar,
        onTimeSelected: (Long, String) -> Unit
    ) {
        val context = requireContext()

        // 先选日期
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                initialCalendar.set(Calendar.YEAR, year)
                initialCalendar.set(Calendar.MONTH, month)
                initialCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // 再选时间
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        initialCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        initialCalendar.set(Calendar.MINUTE, minute)
                        initialCalendar.set(Calendar.SECOND, 0)
                        initialCalendar.set(Calendar.MILLISECOND, 0)

                        val timeMillis = initialCalendar.timeInMillis
                        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        val formatted = format.format(Date(timeMillis))

                        onTimeSelected(timeMillis, formatted)
                    },
                    initialCalendar.get(Calendar.HOUR_OF_DAY),
                    initialCalendar.get(Calendar.MINUTE),
                    true  // 24小时制
                ).show()
            },
            initialCalendar.get(Calendar.YEAR),
            initialCalendar.get(Calendar.MONTH),
            initialCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }



}