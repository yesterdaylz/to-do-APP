package com.example.todoapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.cancelReminder
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.data.entity.Todo
import com.example.todoapp.scheduleReminder
import kotlinx.coroutines.launch

class TodoViewModel(application: Application): AndroidViewModel(application) {
    private val todoDao = TodoDatabase.getInstance(application).todoDao()
    //val todoList: LiveData<List<Todo>> = todoDao.getAllTodos().asLiveData()
    //获取并观察某用户待办事项列表
    fun getTodoList(username: String): LiveData<List<Todo>> =
             todoDao.getTodoByUser(username).asLiveData()
    fun addTodo(todo: Todo){
        viewModelScope.launch {
            val id = todoDao.insert(todo)
            val inserted = todo.copy(id = id)//回填
            //Log.d("MainActivity", "Back button pressed")
            scheduleReminder(getApplication(), inserted)//设置闹钟
        }
    }
    fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            todoDao.update(todo)
            scheduleReminder(getApplication(), todo)   // 更新时重新设置闹钟
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            todoDao.delete(todo)
            cancelReminder(getApplication(),todo)// 删除闹钟
        }
    }

}