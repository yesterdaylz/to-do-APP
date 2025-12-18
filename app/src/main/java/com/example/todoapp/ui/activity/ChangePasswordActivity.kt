package com.example.todoapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.R
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.databinding.ActivityChangePasswordBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userDao = TodoDatabase.getInstance(this).userDao()

        binding.btnSavePwd.setOnClickListener {
            val oldPwd = binding.etOldPwd.text.toString().trim()
            val newPwd = binding.etNewPwd.text.toString().trim()
            val newPwdAgain = binding.etNewPwdAgain.text.toString().trim()

            // 判空
            if (oldPwd.isEmpty() || newPwd.isEmpty() || newPwdAgain.isEmpty()) {
                if (oldPwd.isEmpty()) binding.etOldPwd.error = getString(R.string.hint_old_password)
                if (newPwd.isEmpty()) binding.etNewPwd.error = getString(R.string.hint_new_password)
                if (newPwdAgain.isEmpty()) binding.etNewPwdAgain.error = getString(R.string.hint_confirm_new_password)
                return@setOnClickListener
            }
            if (!isRightPassword(newPwd)) {
                Toast.makeText(this, getString(R.string.error_password_format), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPwd != newPwdAgain) {
                Toast.makeText(this, getString(R.string.error_new_password_mismatch), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPwd == oldPwd) {
                Toast.makeText(this, getString(R.string.error_password_same), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 取当前登录用户
            val prefs = getSharedPreferences("todo_prefs", MODE_PRIVATE)
            val username = prefs.getString("login_user", null)
            if (username.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.error_not_logged_in), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val user = userDao.getByUsername(username)

                if (user == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ChangePasswordActivity, getString(R.string.error_user_not_found), Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                if (user.password != oldPwd) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ChangePasswordActivity, getString(R.string.error_wrong_password), Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }


                val newUser = user.copy(password = newPwd)
                userDao.update(newUser)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChangePasswordActivity, getString(R.string.success_password_changed), Toast.LENGTH_SHORT).show()

                    // 清空登录状态
                    val prefs = getSharedPreferences("todo_prefs", MODE_PRIVATE)
                    prefs.edit().remove("login_user").apply()
                    val intent = Intent(this@ChangePasswordActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }

            }
        }
    }

    private fun isRightPassword(password: String): Boolean {
        if (password.length < 8) return false
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }
}
