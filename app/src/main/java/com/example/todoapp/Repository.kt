package com.example.todoapp

import android.content.Context
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.data.entity.TimeRecord
import com.example.todoapp.data.entity.Todo
import com.example.todoapp.data.entity.User
import com.example.todoapp.logic.motto.QuoteModel
import com.example.todoapp.logic.motto.QuoteNetwork
import kotlinx.coroutines.flow.Flow

class Repository private constructor(context: Context) {
    private val todoDao = TodoDatabase.getInstance(context).todoDao()
    private val userDao = TodoDatabase.getInstance(context).userDao()
    private val timeRecordDao = TodoDatabase.getInstance(context).timeRecordDAO()
    /* ---------------- To do ---------------- */

    fun getTodosByUser(username: String): Flow<List<Todo>> {
        return todoDao.getTodoByUser(username)
    }

    suspend fun insertTodo(todo: Todo): Long {
        return todoDao.insert(todo)
    }

    suspend fun updateTodo(todo: Todo) {
        todoDao.update(todo)
    }

    suspend fun deleteTodo(todo: Todo) {
        todoDao.delete(todo)
    }

    suspend fun setTodoDone(id: Long, done: Boolean) {
        todoDao.setDone(id, done)
    }

    suspend fun getTodoById(id: Long): Todo? {
        return todoDao.getTodoById(id)
    }

    /* ---------------- User ---------------- */

    suspend fun login(username: String, password: String): User? {
        return userDao.login(username, password)
    }

    suspend fun register(user: User): Long {
        return userDao.insert(user)
    }

    suspend fun getUser(username: String): User? {
        return userDao.getByUsername(username)
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }
    /* ---------------- TimeRecord ---------------- */


    fun getAllTimeRecords(username: String): Flow<List<TimeRecord>> {
        return timeRecordDao.getAllRecord(username)
    }
    suspend fun insertTimeRecord(record: TimeRecord): Long {
        return timeRecordDao.insert(record)
    }


    /* ---------------- Network ---------------- */

    suspend fun getRandomQuote(): QuoteModel {
        return QuoteNetwork.randomType()
    }

    companion object {
        @Volatile
        private var INSTANCE: Repository? = null

        fun getInstance(context: Context): Repository {
            return INSTANCE ?: synchronized(this) {
                Repository(context).also { INSTANCE = it }
            }
        }
    }

}