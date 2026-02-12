package com.example.todoapp.ui.auth

sealed class LoginResult {
    data class Success(val username: String) : LoginResult()
    data class Error(val msg: Int) : LoginResult()
}
sealed class LoginUiState {
    data class FillAccount(
        val username: String,
        val password: String,
        val remember: Boolean
    ) : LoginUiState()

    data class AutoLogin(val username: String) : LoginUiState()
}
sealed class RegisterResult {
    data class Success(val msg: Int) : RegisterResult()
    data class Error(val msg: Int) : RegisterResult()
}

sealed class ChangePasswordResult {
    data class Success(val msg: Int) : ChangePasswordResult()
    data class Error(val msg: Int) : ChangePasswordResult()
}

