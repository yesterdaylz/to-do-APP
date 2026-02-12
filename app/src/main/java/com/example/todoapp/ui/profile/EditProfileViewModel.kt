package com.example.todoapp.ui.profile

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.todoapp.R
import com.example.todoapp.Repository
import com.example.todoapp.data.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class EditProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = Repository.getInstance(application)

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    var avatarUri: String? = null
    private val prefs = app.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
    private val context = getApplication<Application>()

    //获取登录用户
    fun loadUser() {
        val username = prefs.getString("login_user",null) ?: run {
            _user.postValue(null)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val u = repository.getUser(username)
            avatarUri = u?.avatarUri
            _user.postValue(u)
        }
    }

    fun updateAvatarFromPicker(uri: Uri, onFailed: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val saved = putUriToFile(uri)
            if (saved == null) {
                onFailed(context.getString(R.string.error_avatar_save_failed))
            } else {
                avatarUri = saved
            }
        }
    }

    fun saveProfile(
        nickname: String?,
        introduction: String?,
        gender: String?,
        region: String?,
        school: String?
    ) {
        val current = _user.value
        if (current == null) {
            _saveResult.postValue(false)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val newUser = current.copy(
                nickname = nickname?.ifBlank { null },
                introduction = introduction?.ifBlank { null },
                gender = gender?.ifBlank { null },
                region = region?.ifBlank { null },
                school = school?.ifBlank { null },
                avatarUri = avatarUri
            )
            repository.updateUser(newUser)
            _user.postValue(newUser)
            _saveResult.postValue(true)
        }
    }
    //转文件保存
    private fun putUriToFile(uri: Uri): String? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
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

