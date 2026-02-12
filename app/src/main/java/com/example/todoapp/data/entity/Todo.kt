package com.example.todoapp.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * 待办实体类，封装待办相关信息，
 *
 * 包含标题、描述、截止日期、提醒时间、完成状态、
 *
 * 创建时间、背景、类别和置顶状态
 *
 * 通过username字段关联用户表
 */

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