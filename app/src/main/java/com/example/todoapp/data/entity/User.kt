package com.example.todoapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户类
 *
 * 用户名、密码、昵称、简介、性别、地区、学校、头像URI
 *
 * 昵称注册时默认为username
 */


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