package com.example.todoapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.data.entity.Todo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodoAdapter (
    private val onItemClick: (Todo) -> Unit,
    private val onDeleteClick: (Todo) -> Unit
): ListAdapter<Todo, TodoAdapter.TodoViewHolder>(DiffCallback()){
    private val backgrounds = listOf(
        R.drawable.bg_todo_1,
        R.drawable.bg_todo_2,
        R.drawable.bg_todo_3,
        R.drawable.bg_todo_4,
        R.drawable.bg_todo_5,
        R.drawable.bg_todo_6,
        R.drawable.bg_todo_7,
        R.drawable.bg_todo_8,
        R.drawable.bg_todo_9,
        R.drawable.bg_todo_10
    )
    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //val cardTodo :com.google.android.material.card.MaterialCardView= itemView.findViewById(R.id.cardTodo)
        val img: ImageView = itemView.findViewById(R.id.img)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvDueTime: TextView = itemView.findViewById(R.id.tvDueTime)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val imgPin: ImageView = itemView.findViewById(R.id.imgPin)
    }
    class DiffCallback : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(oldItem: Todo, newItem: Todo) =
            oldItem.id == newItem.id// 判断项目是否相同

        override fun areContentsTheSame(oldItem: Todo, newItem: Todo) =
            oldItem == newItem// 判断内容是否相同
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_todoitem, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val item = getItem(position)
        holder.tvTitle.text = item.title
        holder.tvDescription.text = item.description ?: ""
        val date = Date(item.dueDay)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        holder.tvDueTime.text = "截止：${format.format(date)}"
        holder.imgPin.visibility = if (item.pin) View.VISIBLE else View.GONE//标记是否置顶
        holder.tvCategory.text = item.category ?: "默认"
        val safeIndex = item.background.coerceIn(0, backgrounds.lastIndex)
        //holder.cardTodo.setBackgroundResource(backgrounds[safeIndex])
        //val drawable = ContextCompat.getDrawable(holder.itemView.context, backgrounds[safeIndex])
        holder.img.setImageResource(backgrounds[safeIndex])

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }
}