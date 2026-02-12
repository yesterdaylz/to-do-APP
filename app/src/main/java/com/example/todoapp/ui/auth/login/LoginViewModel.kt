package com.example.todoapp.ui.auth.login

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.R
import com.example.todoapp.Repository
import com.example.todoapp.ui.auth.LoginResult
import com.example.todoapp.ui.auth.LoginUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = Repository.getInstance(app)
    private val prefs = app.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
    private val context = getApplication<Application>()
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _uiState = MutableLiveData<LoginUiState>()
    val uiState: LiveData<LoginUiState> = _uiState
    //先判断登录状态
    init {
        checkLoginState()
    }
    private fun checkLoginState() {
        val loginUser = prefs.getString("login_user",null)
        //登录用户存在时，自动登录
        if (loginUser != null) {
            _uiState.value = LoginUiState.AutoLogin(loginUser)
            return
        }
        // 检查是否记住密码，记住则自动填充
        val remember = prefs.getBoolean("remember_password", false)
        if (remember) {
            val username = prefs.getString("sv_username", "") ?: ""
            val password = prefs.getString("sv_password", "") ?: ""
            _uiState.value = LoginUiState.FillAccount(username, password, true)
        }
    }
    fun login(username: String,password: String,remember: Boolean) {
        if (username.isBlank()) {
            _loginResult.value =
                LoginResult.Error(R.string.error_username_empty)
            return
        }

        if (password.isBlank()) {
            _loginResult.value =
                LoginResult.Error(R.string.error_password_empty)
            return
        }
        //数据库查询用户
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.login(username, password)
            if (user != null) {
                saveRemember(username, password, remember)
                prefs.edit { putString("login_user", username) }
                _loginResult.postValue(LoginResult.Success(username))
            } else {
                _loginResult.postValue(LoginResult.Error(R.string.error_pwd_username))
            }
        }
    }
    private fun saveRemember(username: String, pwd: String, remember: Boolean) {
        prefs.edit {
            if (remember) {
                putString("sv_username", username)
                putString("sv_password", pwd)
                putBoolean("remember_password", true)
            } else {
                remove("sv_username")
                remove("sv_password")
                putBoolean("remember_password", false)
            }
        }
    }
}