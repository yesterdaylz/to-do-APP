package com.example.todoapp.ui.fragment

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.R
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.data.entity.User
import com.example.todoapp.databinding.FragmentProfileBinding
import com.example.todoapp.ui.activity.EditProfileActivity
import com.example.todoapp.ui.activity.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private lateinit var binding: FragmentProfileBinding
    private var currentUser: User? = null
    //注册图片选择器
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { pickedUri ->
            // 复制到 app 私有目录
            val savedPath = putUriToFile(pickedUri)
            if (savedPath == null) {
                Toast.makeText(requireContext(), "头像保存失败", Toast.LENGTH_SHORT).show()
                return@let
            }

            // 更新数据库中的 avatarUri
            lifecycleScope.launch(Dispatchers.IO) {
                val prefs = requireContext().getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
                val username = prefs.getString("login_user", null)

                username ?: return@launch

                val userDao = TodoDatabase.getInstance(requireContext()).userDao()
                val user = currentUser ?: userDao.getByUsername(username)

                if (user != null) {
                    val newUser = user.copy(avatarUri = savedPath)
                    userDao.update(newUser)
                    currentUser = newUser

                    withContext(Dispatchers.Main) {
                        // 立刻更新头像显示
                        val bitmap = BitmapFactory.decodeFile(savedPath)
                        binding.ivAvatar.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_profile, container, false)
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadInfo()
        binding.ivAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.itemEdit.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }
        binding.itemSettings.setOnClickListener {
            Toast.makeText(requireContext(), "设置 - 敬请期待", Toast.LENGTH_SHORT).show()
        }
        binding.itemHelp.setOnClickListener {
            Toast.makeText(requireContext(), "帮助中心 - 敬请期待", Toast.LENGTH_SHORT).show()
        }
        binding.btnOut.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
            prefs.edit {
                remove("login_user")
            }
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                // 清空回退栈，避免按返回键又回到 MainActivity
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }
    // 在界面重新显示时重新加载信息
    override fun onResume() {
        super.onResume()
        loadInfo()
    }
    // 加载信息
    private fun loadInfo() {
        val prefs = requireContext().getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
        val username = prefs.getString("login_user", null) ?: return
        //io线程获取用户信息
        lifecycleScope.launch(Dispatchers.IO) {
            val userDao = TodoDatabase.getInstance(requireContext()).userDao()
            val user = userDao.getByUsername(username)
            currentUser = user
            user?.let {
                //更新UI
                withContext(Dispatchers.Main) {
                    showUserInfo(it)
                }
            }
        }

    }

    private fun showUserInfo(user: User) {
        binding.tvNickname.text = user.nickname ?: user.username
        binding.tvIntroduction.text = user.introduction ?: "这个人很懒，什么都没写~"
        binding.tvGender.text = getString(R.string.gender_format, user.gender ?: "未知")
        binding.tvRegion.text = getString(R.string.region_format, user.region ?: "未知")
        binding.tvSchool.text =  getString(R.string.school_format, user.school?: "未知")
        //展示头像
        val avatarPath = user.avatarUri
        if (!avatarPath.isNullOrEmpty()) {
            val file = File(avatarPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                binding.ivAvatar.setImageBitmap(bitmap)
            } else {
                binding.ivAvatar.setImageResource(R.mipmap.ic_launcher_round)
            }
        } else {
            binding.ivAvatar.setImageResource(R.mipmap.ic_launcher_round)
        }
    }
    // 将 Uri 转换为文件
    private fun putUriToFile(uri: Uri): String? {
        return try {
            val input = requireContext().contentResolver.openInputStream(uri) ?: return null
            //在私有目录里创建文件
            val file = File(requireContext().filesDir, "avatar_${System.currentTimeMillis()}.jpg")
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