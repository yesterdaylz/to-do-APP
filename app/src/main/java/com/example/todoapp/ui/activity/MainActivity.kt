package com.example.todoapp.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.todoapp.R
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.ui.fragment.FocusFragment
import com.example.todoapp.ui.fragment.ProfileFragment
import com.example.todoapp.ui.fragment.RecordFragment
import com.example.todoapp.ui.fragment.TodoFragment

class MainActivity : AppCompatActivity(), TodoFragment.OnDrawerMenuClickListener {
    companion object {
        private const val REQ_POST_NOTIFICATIONS = 1001
    }
    val fromAlbum = 1
    lateinit var binding: ActivityMainBinding
    private lateinit var username: String
    private val prefs by lazy {
        getSharedPreferences("main_prefs", MODE_PRIVATE)
    }

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
        requestNotificationPermissionIfNeeded()

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        username = intent.getStringExtra("username") ?: ""




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
        val lastTabId = prefs.getInt("last_tab_id", R.id.navigation_todo)
        binding.bottomNavigation.setOnItemSelectedListener {item ->
            prefs.edit().putInt("last_tab_id", item.itemId).apply()
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.navigation_todo -> TodoFragment.newInstance(username)
                R.id.navigation_profile -> ProfileFragment()
                R.id.navigation_focus -> FocusFragment.newInstance(username)
                R.id.navigation_record -> RecordFragment.newInstance(username)
                else -> TodoFragment.newInstance(username)
            }

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()

                 true
        }
        binding.bottomNavigation.selectedItemId = lastTabId
//        if (savedInstanceState == null) {
//            binding.bottomNavigation.selectedItemId = R.id.navigation_todo
//        }
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
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQ_POST_NOTIFICATIONS
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }


}