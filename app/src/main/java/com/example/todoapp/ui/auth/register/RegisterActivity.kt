package com.example.todoapp.ui.auth.register

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.R
import com.example.todoapp.databinding.ActivityRegisterBinding
import com.example.todoapp.ui.auth.RegisterResult
import util.showToast

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val pwd = binding.etPwd.text.toString().trim()
            val pwdAgain = binding.etPwdAgain.text.toString().trim()
            var hasEmpty = false
            viewModel.register(
                username,
                pwd,
               pwdAgain
            )
            if (username.isEmpty()) {
                binding.etUsername.error = getString(R.string.error_username_empty)
                hasEmpty = true
            }
            if (pwd.isEmpty()) {
                binding.etPwd.error = getString(R.string.error_password_empty)
                hasEmpty = true
            }
            if (pwdAgain.isEmpty()) {
                binding.etPwdAgain.error = getString(com.example.todoapp.R.string.hint_confirm_password)
                hasEmpty = true
            }
            if (hasEmpty) return@setOnClickListener
        }

        viewModel.registerResult.observe(this) { result ->
            when (result) {
                is RegisterResult.Success -> {
                    result.msg.showToast(this)
                    finish()
                }
                is RegisterResult.Error -> {
                    result.msg.showToast(this)
                }
            }
        }
    }
}