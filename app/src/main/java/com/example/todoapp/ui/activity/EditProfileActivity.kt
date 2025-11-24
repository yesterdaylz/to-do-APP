package com.example.todoapp.ui.activity

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.R
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.data.entity.User
import com.example.todoapp.databinding.ActivityEditProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditProfileBinding
    private var currentUser: User? = null
    private var avatarUri: String? = null
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = putUriToFile(it)
            avatarUri = savedPath
            binding.ivAvatar.setImageBitmap(BitmapFactory.decodeFile(savedPath))

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadUser()
        binding.ivAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }



    }
    private fun loadUser() {
        val prefs = getSharedPreferences("todo_prefs", MODE_PRIVATE)
        val username = prefs.getString("login_user", null) ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            val userDao = TodoDatabase.getInstance(this@EditProfileActivity).userDao()
            val user = userDao.getByUsername(username)
            currentUser = user

            user?.let {
                avatarUri = it.avatarUri
                withContext(Dispatchers.Main) {
                    binding.etNickname.setText(it.nickname ?: "")
                    binding.etBio.setText(it.introduction ?: "")
                    binding.etGender.setText(it.gender ?: "")
                    binding.etRegion.setText(it.region ?: "")
                    binding.etSchool.setText(it.school ?: "")
                    val avatarPath = it.avatarUri
                    if (!avatarPath.isNullOrEmpty()) {
//                        binding.ivAvatar.setImageURI(Uri.parse(it.avatarUri))
                        val file =  File(avatarPath)
                        if(file.exists()){
                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                            binding.ivAvatar.setImageBitmap(bitmap)
                        }else {
                            binding.ivAvatar.setImageResource(R.mipmap.ic_launcher_round)
                        }
                    }else {
                        binding.ivAvatar.setImageResource(R.mipmap.ic_launcher_round)
                    }
                }
            }
        }
    }

    private fun saveProfile() {
        val user = currentUser
        if (user == null) {
            Toast.makeText(this, "用户信息不存在", Toast.LENGTH_SHORT).show()
            return
        }

        val newUser = user.copy(
            nickname = binding.etNickname.text.toString().ifBlank { null },
            introduction = binding.etBio.text.toString().ifBlank { null },
            gender = binding.etGender.text.toString().ifBlank { null },
            region = binding.etRegion.text.toString().ifBlank { null },
            school = binding.etSchool.text.toString().ifBlank { null },
            avatarUri = avatarUri
        )

        lifecycleScope.launch(Dispatchers.IO) {
            val userDao = TodoDatabase.getInstance(this@EditProfileActivity).userDao()
            userDao.update(newUser)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditProfileActivity, "保存成功", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    private fun putUriToFile(uri: Uri): String? {
        return try {
            //打开输入输出流，创建文件，存到私有目录中
            val input = contentResolver.openInputStream(uri) ?: return null
            val file = File(filesDir, "avatar_${System.currentTimeMillis()}.jpg")
            val output = FileOutputStream(file)

            input.copyTo(output)

            input.close()
            output.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}