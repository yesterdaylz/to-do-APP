package com.example.todoapp.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.R
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.data.entity.User
import com.example.todoapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userDao = TodoDatabase.getInstance(this).userDao()
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val pwd = binding.etPwd.text.toString().trim()
            val pwdAgain = binding.etPwdAgain.text.toString().trim()
            //判空
            if (username.isEmpty() || pwd.isEmpty() || pwdAgain.isEmpty()) {
                if (username.isEmpty()) {
                    binding.etUsername.error = getString(R.string.error_username_empty)
                }
                if (pwd.isEmpty()) {
                    binding.etPwd.error = getString(R.string.error_password_empty)
                }
                if (pwdAgain.isEmpty()) {
                    binding.etPwdAgain.error = getString(R.string.hint_confirm_password)
                }
                return@setOnClickListener
            }
            //判密码规范
            if (!isRightPassword(pwd)) {
                Toast.makeText(this, getString(R.string.error_password_format), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            //判前后密码
            if (pwd != pwdAgain) {
                Toast.makeText(this, getString(R.string.error_password_mismatch), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //存储密码
            lifecycleScope.launch(Dispatchers.IO) {
                val exist = userDao.getByUsername(username)
                withContext(Dispatchers.Main) {
                    if (exist != null) {
                        Toast.makeText(this@RegisterActivity, getString(R.string.error_user_exists), Toast.LENGTH_SHORT)
                            .show()

                    } else {
                        userDao.insert(User(username = username, password = pwd))
                        Toast.makeText(this@RegisterActivity, getString(R.string.success_register), Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                }
            }
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }
    //合法密码
    private fun isRightPassword(password: String): Boolean {
        if(password.length < 8){
            return false
        }
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }
}