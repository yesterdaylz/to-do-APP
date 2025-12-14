package com.example.todoapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userDao = TodoDatabase.getInstance(this).userDao()
        val loginUser = prefs.getString("login_user",null)
        //登陆过的用户自动登录
        if(loginUser != null){
            goToMain(loginUser)
            finish()
            return
        }
        //判断用户之前是否选中记住密码
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
            //启动协程，与当前Activity生命周期绑定，避免内存泄漏
            lifecycleScope.launch{
                val user = userDao.login(username, pwd )
                if(user != null){
                    rememberPassword(username,pwd,binding.remenberPass)
                    //保存已登录的用户
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
        //跳转到注册界面
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
    //记住密码
    private fun rememberPassword(username: String,pwd: String,rm: CheckBox){
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
    //带上用户名，前往主页
    private fun goToMain(username: String){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("username",username)//传递用户名
        startActivity(intent)
    }
}