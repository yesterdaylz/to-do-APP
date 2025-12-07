package com.example.todoapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.todoapp.data.dao.TimeRecordDao
import com.example.todoapp.data.dao.TodoDao
import com.example.todoapp.data.dao.UserDao
import com.example.todoapp.data.entity.TimeRecord
import com.example.todoapp.data.entity.Todo
import com.example.todoapp.data.entity.User

@Database(entities = [Todo::class, User::class,TimeRecord::class], version = 5)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun userDao(): UserDao
    abstract fun timeRecordDAO(): TimeRecordDao
    companion object {
        @Volatile private var INSTANCE: TodoDatabase? = null
        fun getInstance(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}