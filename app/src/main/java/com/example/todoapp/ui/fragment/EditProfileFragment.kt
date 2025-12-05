package com.example.todoapp.ui.fragment

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.todoapp.R
import com.example.todoapp.data.database.TodoDatabase
import com.example.todoapp.data.entity.User
import com.example.todoapp.databinding.FragmentEditProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private var currentUser: User? = null
    private var avatarUri: String? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = putUriToFile(it)
            avatarUri = savedPath
            if (savedPath != null) {
                binding.ivAvatar.setImageBitmap(BitmapFactory.decodeFile(savedPath))
            } else {
                Toast.makeText(requireContext(), "头像保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUser()

        binding.ivAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadUser() {
        val prefs = requireContext().getSharedPreferences("todo_prefs", android.content.Context.MODE_PRIVATE)
        val username = prefs.getString("login_user", null) ?: return

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val userDao = TodoDatabase.getInstance(requireContext()).userDao()
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
            }
        }
    }

    private fun saveProfile() {
        val user = currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "用户信息不存在", Toast.LENGTH_SHORT).show()
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

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val userDao = TodoDatabase.getInstance(requireContext()).userDao()
            userDao.update(newUser)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show()
                // 关闭当前编辑页面（返回上一层 ProfileFragment）
                parentFragmentManager.popBackStack()
            }
        }
    }

    // 将 Uri 转为文件路径，保存到私有目录
    private fun putUriToFile(uri: Uri): String? {
        return try {
            val input = requireContext().contentResolver.openInputStream(uri) ?: return null
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
