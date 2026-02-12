package com.example.todoapp.ui.auth.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.R
import com.example.todoapp.Repository
import com.example.todoapp.data.entity.User
import com.example.todoapp.ui.auth.RegisterResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import util.isRightPassword

class RegisterViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = Repository.getInstance(app)
    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    fun register(username: String, pwd: String, pwdAgain: String) {
        //判空
        if(username.isBlank()) {
            _registerResult.value =
                RegisterResult.Error(R.string.error_username_empty)
            return
        }
        if(pwd.isBlank()) {
            _registerResult.value =
                RegisterResult.Error(R.string.error_password_empty)
            return
        }

        if(pwdAgain.isBlank()) {
            _registerResult.value =
                RegisterResult.Error(R.string.hint_confirm_password)
            return
        }

        //密码合法性检查：至少8位，包含字母和数字
        if(!isRightPassword(pwd)) {
            _registerResult.value =
                RegisterResult.Error(R.string.error_password_format)
            return
        }

        //两次密码一致检查
        if (pwd != pwdAgain) {
            _registerResult.value =
                RegisterResult.Error(R.string.error_password_mismatch)
            return
        }

       //数据库查询用户是否存在，注册新用户
        viewModelScope.launch(Dispatchers.IO) {
            val exist = repository.getUser(username)
            if (exist != null) {
                _registerResult.postValue(
                    RegisterResult.Error(R.string.error_user_exists)
                )
            } else {
                repository.register(User(username = username, password = pwd))
                _registerResult.postValue(RegisterResult.Success(R.string.success_register))
            }
        }
    }
}