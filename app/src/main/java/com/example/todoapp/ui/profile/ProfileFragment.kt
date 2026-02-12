package com.example.todoapp.ui.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todoapp.MyApp.Companion.ABOUTURL
import com.example.todoapp.R
import com.example.todoapp.data.entity.User
import com.example.todoapp.databinding.FragmentProfileBinding
import com.example.todoapp.ui.auth.changePwd.ChangePwdActivity
import com.example.todoapp.ui.auth.login.LoginActivity
import util.showToast
import java.io.File


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    //注册图片选择器
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { pickedUri ->
            viewModel.updateAvatar(pickedUri) { msg ->
                msg.showToast(this.requireContext())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //监听用户信息变化
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let { showUserInfo(it) }
        }
        //点击头像，设置头像
        binding.ivAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        //编辑个人信息页跳转
        binding.itemEdit.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }
        //修改密码页跳转
        binding.itemChangePwd.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePwdActivity::class.java))
        }
        //网页跳转
        binding.itemAbout.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, ABOUTURL.toUri())
            try {
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                R.string.error_open_web_failed.showToast(requireContext())
            }
        }
        binding.btnOut.setOnClickListener {
            viewModel.removeLoginStatus()
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK// 清空回退栈
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }


    override fun onResume() {
        super.onResume()
        viewModel.loadUser()
    }

    private fun showUserInfo(user: User) {
        //个人信息
        binding.tvNickname.text = user.nickname ?: user.username
        binding.tvIntroduction.text = user.introduction ?: getString(R.string.default_introduction)
        binding.tvGender.text = getString(R.string.gender_format, user.gender ?: getString(R.string.unknown))
        binding.tvRegion.text = getString(R.string.region_format, user.region ?: getString(R.string.unknown))
        binding.tvSchool.text =  getString(R.string.school_format, user.school?: getString(R.string.unknown))
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
