package com.example.todoapp.ui.auth.changePwd

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.R
import com.example.todoapp.databinding.ActivityChangePasswordBinding
import com.example.todoapp.ui.auth.ChangePasswordResult
import com.example.todoapp.ui.auth.login.LoginActivity
import util.showToast

class ChangePwdActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private val viewModel: ChangePwdViewmodel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSavePwd.setOnClickListener {
            val oldPwd = binding.etOldPwd.text.toString().trim()
            val newPwd = binding.etNewPwd.text.toString().trim()
            val newPwdAgain = binding.etNewPwdAgain.text.toString().trim()
            var hasEmpty = false
            viewModel.changePassword(oldPwd, newPwd, newPwdAgain)
            if (oldPwd.isEmpty()) {
                binding.etOldPwd.error = getString(R.string.hint_old_password)
                hasEmpty = true
            }
            if (newPwd.isEmpty()) {
                binding.etNewPwd.error = getString(R.string.hint_new_password)
                hasEmpty = true
            }
            if (newPwdAgain.isEmpty()) {
                binding.etNewPwdAgain.error = getString(R.string.hint_confirm_new_password)
                hasEmpty = true
            }
            if (hasEmpty) return@setOnClickListener
        }

        viewModel.result.observe(this) { result ->
            when (result) {
                is ChangePasswordResult.Success -> {
                   result.msg.showToast(this)
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                is ChangePasswordResult.Error -> {
                    result.msg.showToast(this)
                }
            }
        }
    }
}