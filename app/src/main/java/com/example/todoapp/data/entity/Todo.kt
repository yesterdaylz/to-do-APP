package com.example.todoapp.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(tableName = "todo")
data class Todo(
    @PrimaryKey(autoGenerate = true) val  id : Long = 0,
    val username: String,
    val title: String,
    val description: String?,
    val dueDay: Long,
    val remindTime: Long?,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val background: Int = 0,
    val category: String? = "默认",
    val pin: Boolean = false
): Parcelable