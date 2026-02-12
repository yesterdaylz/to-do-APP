package com.example.todoapp.ui.todo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.R
import com.example.todoapp.Repository
import com.example.todoapp.data.entity.Todo
import kotlinx.coroutines.launch
import util.cancelReminder
import util.scheduleReminder
import util.showToast

class TodoViewModel(application: Application): AndroidViewModel(application) {
    //private val todoDao = TodoDatabase.getInstance(application).todoDao()
    //val todoList: LiveData<List<Todo>> = todoDao.getAllTodos().asLiveData()
    private val repository = Repository.getInstance(application)
    fun getTodoList(username: String): LiveData<List<Todo>> =
        repository.getTodosByUser(username).asLiveData()
    fun addTodo(todo: Todo){
        viewModelScope.launch {
            try {
                val id = repository.insertTodo(todo)
                val inserted = todo.copy(id = id)
                //Log.d("MainActivity", "Back button pressed")
                scheduleReminder(getApplication(), inserted)
            } catch (e: Exception) {
                R.string.error_add_todo_failed.showToast(getApplication(),e.message)
            }
        }
    }
    fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.updateTodo(todo)
                // 更新时重新设置闹钟
                scheduleReminder(getApplication(), todo)
            } catch (e: Exception) {
                R.string.error_update_todo_failed.showToast(getApplication(),e.message)
            }
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.deleteTodo(todo)
                cancelReminder(getApplication(), todo)// 删除闹钟
            } catch (e: Exception) {
                R.string.error_delete_todo_failed.showToast(getApplication(),e.message)
            }
        }
    }
    fun toggleDone(todo: Todo) {
        viewModelScope.launch {
            try {
                val newDone = !todo.done
                repository.setTodoDone(todo.id, newDone)
                if (newDone) {
                    cancelReminder(getApplication(), todo)
                } else {
                    scheduleReminder(getApplication(), todo.copy(done = false))
                }
            } catch (e: Exception) {
                R.string.error_toggle_done_failed.showToast(getApplication(),e.message)
            }
        }
    }


}