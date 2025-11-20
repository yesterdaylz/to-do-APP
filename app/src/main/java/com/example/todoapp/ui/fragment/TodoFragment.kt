package com.example.todoapp.ui.fragment

//import android.app.ProgressDialog.show
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todoapp.R
import com.example.todoapp.adapter.TodoAdapter
import com.example.todoapp.databinding.FragmentTodoBinding
import com.example.todoapp.ui.viewmodel.TodoViewModel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [TodoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TodoFragment : Fragment(R.layout.fragment_todoitem) {
    private val viewModel : TodoViewModel  by viewModels()
    private lateinit var adapter: TodoAdapter
    private lateinit var username: String
    interface OnDrawerMenuClickListener{
        fun onDrawerMenuClicked ()
    }
    private var drawerMenuClickListener: OnDrawerMenuClickListener? = null
    // TODO: Rename and change types of parameters
    //private var param1: String? = null
    //private var param2: String? = null
    private lateinit var binding: FragmentTodoBinding
    override fun onAttach(context: Context) {
        super.onAttach(context)
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
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTodoBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //顶部栏抽屉
        binding.toolbar.setNavigationIcon(R.mipmap.ic_drawer)
        binding.toolbar.setNavigationOnClickListener {
            drawerMenuClickListener?.onDrawerMenuClicked()
        }
        //顶部栏右部分
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add -> {
                    Toast.makeText(requireContext(), "Add", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.more -> {
                    Toast.makeText(requireContext(), "Settings", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> {
                    false
                }
            }
        }
        adapter = TodoAdapter(
            onItemClick = { todo ->
                AddTodoDialogFragment.newInstance(todo,username)
                    .show(childFragmentManager, "AddDialogFragment")
            },
            onDeleteClick = { todo ->
                viewModel.deleteTodo(todo)
            }
        )
        binding.rvTodo.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvTodo.adapter = adapter
        viewModel.getTodoList(username).observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
        binding.fabAddTodo.setOnClickListener {
            AddTodoDialogFragment.newInstance(null,username).show(
                parentFragmentManager,
                "add_todo"
            )
        }


    }







    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         *
         *
         * @return A new instance of fragment TodoFragment.
         */
        // TODO: Rename and change types and number of parameters
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