package com.example.todoapp.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.R
import com.example.todoapp.cancelReminder
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.data.entity.Todo
import com.example.todoapp.scheduleReminder
import kotlinx.coroutines.launch


class TodoViewModel(application: Application): AndroidViewModel(application) {
    private val todoDao = TodoDatabase.getInstance(application).todoDao()
    //val todoList: LiveData<List<Todo>> = todoDao.getAllTodos().asLiveData()
    fun getTodoList(username: String): LiveData<List<Todo>> =
        todoDao.getTodoByUser(username).asLiveData()
    fun addTodo(todo: Todo){
        viewModelScope.launch {
            try {
                val id = todoDao.insert(todo)
                val inserted = todo.copy(id = id)//回填
                //Log.d("MainActivity", "Back button pressed")
                scheduleReminder(getApplication(), inserted)//设置闹钟
            } catch (e: Exception) {
                androidx.core.content.ContextCompat.getSystemService(
                    getApplication(), android.app.NotificationManager::class.java
                )?.let {
                    Toast.makeText(getApplication(),
                        getApplication<Application>()
                            .getString(R.string.error_add_todo_failed, e.message),
                        Toast.LENGTH_SHORT).show()

                }
            }
        }
    }
    fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            try {
                todoDao.update(todo)
                scheduleReminder(getApplication(), todo)   // 更新时重新设置闹钟
            } catch (e: Exception) {
                Toast.makeText(getApplication(),
                    getApplication<Application>()
                        .getString(R.string.error_update_todo_failed, e.message),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            try {
                todoDao.delete(todo)
                cancelReminder(getApplication(),todo)// 删除闹钟
            } catch (e: Exception) {
                Toast.makeText(getApplication(),
                    getApplication<Application>()
                        .getString(R.string.error_delete_todo_failed, e.message),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun toggleDone(todo: Todo) {
        viewModelScope.launch {
            try {
                val newDone = !todo.done
                todoDao.setDone(todo.id, newDone)
                if (newDone) {
                    cancelReminder(getApplication(), todo)
                } else {
                    scheduleReminder(getApplication(), todo.copy(done = false))
                }
            } catch (e: Exception) {
                Toast.makeText(getApplication(),
                    getApplication<Application>()
                        .getString(R.string.error_toggle_done_failed, e.message),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }


}
