package com.example.todoapp.ui.auth.changePwd

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.R
import com.example.todoapp.Repository
import com.example.todoapp.ui.auth.ChangePasswordResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import util.isRightPassword

class ChangePwdViewmodel(app: Application): AndroidViewModel(app) {

    private val repository = Repository.getInstance(app)
    private val prefs = app.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
    //private val context = getApplication<Application>()

    private val _result = MutableLiveData<ChangePasswordResult>()
    val result: LiveData<ChangePasswordResult> = _result

    fun changePassword(oldPwd: String, newPwd: String, newPwdAgain: String) {
        if (oldPwd.isBlank() || newPwd.isBlank() || newPwdAgain.isBlank()) {
            _result.value = ChangePasswordResult.Error(R.string.error_password_empty)
            return
        }

        if (!isRightPassword(newPwd)) {
            _result.value = ChangePasswordResult.Error(R.string.error_password_format)
            return
        }

        if (newPwd != newPwdAgain) {
            _result.value = ChangePasswordResult.Error(R.string.error_new_password_mismatch)
            return
        }

        if (newPwd == oldPwd) {
            _result.value = ChangePasswordResult.Error(R.string.error_password_same)
            return
        }

        // 取当前登录用户
        val username = prefs.getString("login_user", null)
        if (username.isNullOrEmpty()) {
            _result.value = ChangePasswordResult.Error(R.string.error_not_logged_in)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUser(username)
            if (user == null) {
                _result.postValue(ChangePasswordResult.Error(R.string.error_user_not_found))
                return@launch
            }

            if (user.password != oldPwd) {
                _result.postValue(ChangePasswordResult.Error(R.string.error_wrong_password))
                return@launch
            }

            // 更新密码
            repository.updateUser(user.copy(password = newPwd))

            // 清空登录状态（保持和你原来一致：只清 login_user）
            prefs.edit { remove("login_user") }

            _result.postValue(ChangePasswordResult.Success(R.string.success_password_changed))
        }
    }

    // 密码合法性检查：至少8位，包含字母和数字

}