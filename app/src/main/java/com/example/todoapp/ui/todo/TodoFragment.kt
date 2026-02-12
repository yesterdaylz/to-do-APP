package com.example.todoapp.ui.todo

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.R
import com.example.todoapp.adapter.TodoAdapter
import com.example.todoapp.data.entity.Todo
import com.example.todoapp.databinding.FragmentTodoBinding
import com.example.todoapp.ui.dialog.AddTodoDialogFragment
import com.example.todoapp.ui.dialog.ImportantDialogFragment
import com.example.todoapp.ui.main.OnDrawerMenuClickListener
import util.showToast

class TodoFragment : Fragment(R.layout.fragment_todoitem) {
    private val viewModel : TodoViewModel by viewModels()
    private lateinit var adapter: TodoAdapter
    private lateinit var username: String
    private var isFirstCgSelected = true
    private var allTodo :List<Todo> = emptyList()

    private var drawerMenuClickListener: OnDrawerMenuClickListener? = null
    private var _binding: FragmentTodoBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            username = it.getString(ARG_PARAM1) ?:""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodoBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //顶部栏抽屉
        binding.toolbar.setNavigationIcon(R.mipmap.ic_drawer)
        binding.toolbar.setNavigationOnClickListener {
            drawerMenuClickListener?.onDrawerMenuClicked()
        }
        //顶部栏菜单
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                //夜间模式
                R.id.night -> {
                    val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    when (currentNightMode) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        }
                        else -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        }
                    }
                    true
                }

                R.id.important -> {
                    ImportantDialogFragment().show(childFragmentManager, "ImportantDialogFragment")
                    true
                }
                R.id.lang -> {
                    toggleLanguage()
                    true
                }

                else -> {
                    false
                }
            }
        }
        adapter = TodoAdapter(
            //点击待办事项编辑
            onItemClick = { todo ->
                AddTodoDialogFragment.newInstance(todo)
                    .show(parentFragmentManager, "AddDialogFragment")
            },
            //删除
            onDeleteClick = { todo ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.todo_delete_title)
                    .setMessage(getString(R.string.todo_delete_message, todo.title))
                    .setPositiveButton(R.string.todo_delete_confirm) { _, _ ->
                        viewModel.deleteTodo(todo)
                    }
                    .setNegativeButton(R.string.todo_delete_cancel, null)
                    .show()
            },
            onToggleDone = { todo ->
                viewModel.toggleDone(todo)
            }

        )
        binding.rvTodo.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTodo.adapter = adapter
        val categories = listOf(
            getString(R.string.category_all),
            getString(R.string.category_default),
            getString(R.string.category_study),
            getString(R.string.category_work),
            getString(R.string.category_life),
            getString(R.string.category_other)
        )
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spCategory.adapter = spinnerAdapter
        binding.spCategory.onItemSelectedListener= object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //忽略第一次选择
                if (isFirstCgSelected) {
                    isFirstCgSelected = false
                    return
                }
                val selectedCategory = categories[position]
                categoryFilter(selectedCategory)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                categoryFilter(getString(R.string.category_all))
            }
        }
        //观察 LiveData，自动更新界面
        viewModel.getTodoList(username).observe(viewLifecycleOwner) { list ->
            allTodo =list
            adapter.submitList(list)
        }
        //悬浮按钮
        binding.fabAddTodo.setOnClickListener {
            AddTodoDialogFragment.newInstance(null).show(
                parentFragmentManager,
                "add_todo"
            )
        }
    }
    private fun categoryFilter(category: String) {
        val filtered = when (category) {
            getString(R.string.category_all) -> allTodo
            else -> allTodo.filter { it.category == category }
        }

        // 如果筛选结果为空，展示提示
        if (filtered.isEmpty()) {
            R.string.todo_filter_no_results.showToast(requireContext())

        }
        adapter.submitList(filtered)
    }
    private fun toggleLanguage() {
        //获取当前应用的语言设置，语言环境列表中的第一个（默认）语言环境
        val currentLocale = resources.configuration.locales[0].language
        val newLanguageTag = if (currentLocale == "zh") "en" else "zh-CN"
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(newLanguageTag)
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PARAM1 = "username"
        @JvmStatic
        fun newInstance(username: String) =
            TodoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, username)
                }
            }
    }
}