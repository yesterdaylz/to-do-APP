package com.example.todoapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.todoapp.data.entity.User

@Dao
interface UserDao {
    @Query ("SELECT * FROM user WHERE username =:username LIMIT 1")
    suspend fun getByUsername(username: String): User?
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long
    @Query("SELECT * FROM user WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String,password: String): User?
    @Update
    suspend fun update(user: User)
}