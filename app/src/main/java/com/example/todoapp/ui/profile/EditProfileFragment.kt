package com.example.todoapp.ui.profile

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentEditProfileBinding
import util.showToast
import java.io.File

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModels()
    //注册图片选择器
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateAvatarFromPicker(it) { msg ->
                msg.showToast(requireContext())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //监听用户信息变化
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
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
        //监听保存结果，保存后返回个人信息页
        viewModel.saveResult.observe(viewLifecycleOwner) { ok ->
            if (ok == true) {
                R.string.save_succeed.showToast(requireContext())
                parentFragmentManager.popBackStack()
            } else {
                R.string.error_user_info_not_found.showToast(requireContext())
            }
        }
        //选图片
        binding.ivAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        //保存
        binding.btnSave.setOnClickListener {
            viewModel.saveProfile(
                binding.etNickname.text.toString(),
                binding.etBio.text.toString(),
                binding.etGender.text.toString(),
                binding.etRegion.text.toString(),
                binding.etSchool.text.toString()
            )
        }
        viewModel.loadUser()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}