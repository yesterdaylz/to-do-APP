package com.example.todoapp.adapter

import android.graphics.Paint
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
    private val onDeleteClick: (Todo) -> Unit,
    private val onToggleDone: (Todo) -> Unit
): ListAdapter<Todo, TodoAdapter.TodoViewHolder>(DiffCallback()){
    private val backgrounds = listOf(
        R.drawable.bg_todo_7,
        R.drawable.bg_todo_2,
        R.drawable.bg_todo_3,
        R.drawable.bg_todo_10,
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
        if (item.done) {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvDescription.paintFlags = holder.tvDescription.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.alpha = 0.6f
        } else {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvDescription.paintFlags = holder.tvDescription.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.itemView.alpha = 1.0f
        }
        val date = Date(item.dueDay)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        holder.tvDueTime.text = holder.itemView.context.getString(R.string.due_time_prefix, format.format(date))
        holder.imgPin.visibility = if (item.pin) View.VISIBLE else View.GONE//标记是否置顶
        holder.tvCategory.text = item.category ?: holder.itemView.context.getString(R.string.category_default)
        val safeIndex = item.background.coerceIn(0, backgrounds.lastIndex)
        holder.img.setImageResource(backgrounds[safeIndex])
        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
        holder.itemView.setOnLongClickListener {
            onToggleDone(item)
            true
        }

    }
}