package com.example.todoapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_record")
data class TimeRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val mode: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Int,
    val tomatoCount: Int = 1
)
