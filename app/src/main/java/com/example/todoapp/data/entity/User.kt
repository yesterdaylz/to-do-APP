package com.example.todoapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val password: String,
    val nickname: String? = null,
    val introduction: String? = null,
    val gender: String? = null,
    val region: String? = null,
    val school: String? = null,
    val avatarUri: String? = null
)