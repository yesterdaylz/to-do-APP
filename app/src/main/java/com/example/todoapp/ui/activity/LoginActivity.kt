package com.example.todoapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.R
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private val prefs by lazy {
        getSharedPreferences("todo_prefs",MODE_PRIVATE)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userDao = TodoDatabase.getInstance(this).userDao()
        val loginUser = prefs.getString("login_user",null)
        if(loginUser != null){
            goToMain(loginUser)
            finish()
            return
        }
        val remember = prefs.getBoolean("remember_password",false)
        if (remember){
            binding.etUsername.setText(prefs.getString("sv_username",""))
            binding.etPwd.setText(prefs.getString("sv_password",""))
            binding.remenberPass.isChecked = true
        }
        binding.btnLogin.setOnClickListener{
            val username = binding.etUsername.text.toString().trim()
            val pwd = binding.etPwd.text.toString().trim()
            if(username.isEmpty() || pwd.isEmpty()){
                Toast.makeText(this,"账号或密码不能为空",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch{//->启动的协程会与组件的生周期自动绑定
                val user = userDao.login(username, pwd )
                if(user != null){
                    rememberPassword(username,pwd,binding.remenberPass)
                    prefs.edit{
                        putString("login_user",username)
                    }


                    goToMain(username)
                    finish()
                }else{
                    Toast.makeText(this@LoginActivity,"账号或密码错误",Toast.LENGTH_SHORT).show()
                }

            }
        }
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
    private fun rememberPassword(username: String,pwd: String,rm : CheckBox){
        prefs.edit {
            if (rm.isChecked) {
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
    private fun goToMain(username: String){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("username",username)//传递用户名
        startActivity(intent)
    }
}