package com.example.todoapp.ui.profile

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.R
import com.example.todoapp.Repository
import com.example.todoapp.data.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository.getInstance(application)
    private val prefs = appContext.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val appContext get() = getApplication<Application>().applicationContext

    private fun getLoginUsername(): String? {
        return prefs.getString("login_user", null)
    }
    fun removeLoginStatus() {
        prefs.edit { remove("login_user") }
    }
    //加载用户信息
    fun loadUser() {
        val username = getLoginUsername() ?: run {
            _user.postValue(null)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val u = repository.getUser(username)
            _user.postValue(u)
        }
    }
    //更新头像
    fun updateAvatar(uri: Uri, onFailed: (String) -> Unit) {
        val username = getLoginUsername() ?: run {
            onFailed(appContext.getString(R.string.error_user_info_not_found))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val savedPath = putUriToFile(uri)
            if (savedPath == null) {
                onFailed(appContext.getString(R.string.error_avatar_save_failed))
                return@launch
            }
            val user = repository.getUser(username)
            if (user != null) {
                val newUser = user.copy(avatarUri = savedPath)
                repository.updateUser(newUser)
                _user.postValue(newUser)
            } else {
                onFailed(appContext.getString(R.string.error_user_info_not_found))
            }
        }
    }
    //保存文件
    private fun putUriToFile(uri: Uri): String? {
        return try {
            val input = appContext.contentResolver.openInputStream(uri) ?: return null
            val file = File(appContext.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
            input.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}