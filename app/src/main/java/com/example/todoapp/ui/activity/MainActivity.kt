package com.example.todoapp.ui.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.todoapp.R
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.ui.fragment.ApplicationFragment
import com.example.todoapp.ui.fragment.ProfileFragment
import com.example.todoapp.ui.fragment.TodoFragment

class MainActivity : AppCompatActivity(), TodoFragment.OnDrawerMenuClickListener {
    val fromAlbum = 1
    lateinit var binding: ActivityMainBinding
    private val choosePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val imageView = findViewById<ImageView>(R.id.imageView)
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    val bitmap = getBitmapFromUri(uri)
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val headerView = binding.navView.getHeaderView(0)
        // 从头部视图中查找ImageView
        val headerImageView = headerView.findViewById<ImageView>(R.id.imageView)
        headerImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            // 指定只显示图片
            intent.type = "image/*"
            choosePictureLauncher.launch(intent)
        }
        binding.bottomNavigation.setOnItemSelectedListener {item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.navigation_todo-> {
                    selectedFragment = TodoFragment()
                }
                R.id.navigation_profile -> {
                    selectedFragment = ProfileFragment()
                }
                R.id.navigation_application -> {
                    selectedFragment = ApplicationFragment()
                }
            }
            selectedFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, it)
                    .commit()
            }
            true
        }
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.navigation_todo
        }
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageView = findViewById<ImageView>(R.id.imageView)
        when (requestCode) {
            fromAlbum -> {
                if (resultCode == RESULT_OK && data != null) {
                    data.data?.let { uri ->
                        val bitmap = getBitmapFromUri(uri)
                        imageView.setImageBitmap(bitmap)
                    }
                }
            }
        }


    }
    override fun onDrawerMenuClicked() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }
    private fun getBitmapFromUri(uri: Uri) = contentResolver
        .openFileDescriptor(uri, "r")?.use {
            BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
        }


}