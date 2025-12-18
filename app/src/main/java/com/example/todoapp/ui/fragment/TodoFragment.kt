package com.example.todoapp.ui.fragment

//import android.app.ProgressDialog.show
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todoapp.R
import com.example.todoapp.adapter.TodoAdapter
import com.example.todoapp.data.entity.Todo
import com.example.todoapp.databinding.FragmentTodoBinding
import com.example.todoapp.ui.viewmodel.TodoViewModel

class TodoFragment : Fragment(R.layout.fragment_todoitem) {
    private val viewModel : TodoViewModel  by viewModels()
    private lateinit var adapter: TodoAdapter
    private lateinit var username: String
    private var isFirstCgSelected = true
    private var allTodo :List<Todo> = emptyList()
    //搞个回调接口
    interface OnDrawerMenuClickListener{
        fun onDrawerMenuClicked ()
    }
    private var drawerMenuClickListener: OnDrawerMenuClickListener? = null
    private var _binding: FragmentTodoBinding? = null
    private val binding get() = _binding!!
    override fun onAttach(context: Context) {
        super.onAttach(context)
        //判断是否实现接口
        if (context is OnDrawerMenuClickListener){
            drawerMenuClickListener = context
        }else{
            throw RuntimeException(context.toString() + "must implement OnDrawerMenuClickListener")
        }
    }

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
        // Inflate the layout for this fragment
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

                R.id.more -> {
                    val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                    when (currentNightMode) {
                        android.content.res.Configuration.UI_MODE_NIGHT_YES -> {
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

                else -> {
                    false
                }
            }
        }
        adapter = TodoAdapter(
            //点击待办事项编辑
            onItemClick = { todo ->
                AddTodoDialogFragment.newInstance(todo,username)
                    .show(parentFragmentManager, "AddDialogFragment")
            },
            //删除
            onDeleteClick = { todo ->
                AlertDialog.Builder(requireContext())
                    .setTitle("删除待办？")
                    .setMessage("确定要删除 '${todo.title}' 吗/(ㄒoㄒ)/~~？")
                    .setPositiveButton("删除") { _, _ ->
                        viewModel.deleteTodo(todo)
                    }
                    .setNegativeButton("我不小心点到了", null)
                    .show()
            },
            onToggleDone = { todo ->
                viewModel.toggleDone(todo)
            }

        )
        binding.rvTodo.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvTodo.adapter = adapter
        val categories = listOf("全部","默认" ,"学习" ,"工作" ,"生活" ,"其他")
        binding.spCategory.dropDownWidth = 150 * resources.displayMetrics.density.toInt()
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spCategory.adapter = spinnerAdapter
        //实现接口，设置监听器
        binding.spCategory.onItemSelectedListener= object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //忽略第一次选择，不然启动时这玩意会自己弹出来
                if (isFirstCgSelected) {
                    isFirstCgSelected = false
                    return
                }
                val selectedCategory = categories[position]
                categoryFilter(selectedCategory)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 不选就当“全部”
                categoryFilter("全部")
            }
        }
        //观察 LiveData，自动更新界面
        viewModel.getTodoList(username).observe(viewLifecycleOwner) { list ->
            allTodo =list
            adapter.submitList(list)
        }
        //悬浮按钮
        binding.fabAddTodo.setOnClickListener {
            AddTodoDialogFragment.newInstance(null,username).show(
                parentFragmentManager,
                "add_todo"
            )
        }
    }
    private fun categoryFilter(category: String) {
        val filtered = when (category) {
            "全部" -> allTodo
            else -> allTodo.filter { it.category == category }
        }

        // 如果筛选结果为空，展示提示
        if (filtered.isEmpty()) {
            Toast.makeText(requireContext(), "没有找到符合条件的待办事项", Toast.LENGTH_SHORT).show()
        }

        // 更新 adapter
        adapter.submitList(filtered)
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