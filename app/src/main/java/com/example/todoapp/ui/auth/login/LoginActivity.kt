package com.example.todoapp.ui.auth.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.todoapp.R
import com.example.todoapp.databinding.ActivityLoginBinding
import com.example.todoapp.ui.main.MainActivity
import com.example.todoapp.ui.auth.LoginResult
import com.example.todoapp.ui.auth.LoginUiState
import com.example.todoapp.ui.auth.register.RegisterActivity
import util.showToast

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
//    private val viewModel by lazy {
//        ViewModelProvider(this)[LoginViewModel::class.java]
//    }
    private val viewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //登录按钮
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val pwd = binding.etPwd.text.toString().trim()
            var hasEmpty = false
            viewModel.login(
                username,
                pwd,
                binding.remember.isChecked
            )
            if (username.isEmpty()) {
                binding.etUsername.error = getString(R.string.error_username_empty)
                hasEmpty = true
            }
            if (pwd.isEmpty()) {
                binding.etPwd.error = getString(R.string.error_password_empty)
                hasEmpty = true
            }
            if (hasEmpty) return@setOnClickListener
        }
        //注册按钮
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        // 监听登录结果
        viewModel.loginResult.observe(this) { result ->
            when(result){
                is LoginResult.Success ->{
                    goToMain(result.username)
                    finish()
                }
                is LoginResult.Error -> {
                   result.msg.showToast(this)
                }
            }
        }

        //  监听（自动登录&填充账号）
        viewModel.uiState.observe(this) { state ->
            when(state){
                is LoginUiState.AutoLogin -> {
                    goToMain(state.username)
                    finish()
                }
                is LoginUiState.FillAccount -> {
                    binding.etUsername.setText(state.username)
                    binding.etPwd.setText(state.password)
                    binding.remember.isChecked = state.remember
                }
            }
        }
    }

    private fun goToMain(username: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }
}