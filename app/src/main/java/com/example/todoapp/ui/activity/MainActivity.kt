package com.example.todoapp.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
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
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), TodoFragment.OnDrawerMenuClickListener {
    private var lastBackPressedTime = 0L
    companion object {
        private const val REQ_POST_NOTIFICATIONS = 1001
    }
    lateinit var binding: ActivityMainBinding
    private lateinit var username: String
    private lateinit var headerImageView: ImageView

    // 每个用户固定一个封面文件名
    private fun safeUserKey() = username.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    //同用户反复选择覆盖
    private fun coverFile() = File(filesDir, "cover_${safeUserKey()}.jpg")

    private val choosePictureLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@registerForActivityResult

            val ok = copyUriToFile(uri, coverFile())
            if (!ok) {
                Toast.makeText(this, "封面保存失败", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            // 回显封面
            headerImageView.setImageBitmap(
                BitmapFactory.decodeFile(coverFile().absolutePath)
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        username = intent.getStringExtra("username") ?: ""
        val headerView = binding.navView.getHeaderView(0)
        headerImageView = headerView.findViewById(R.id.imageView)
        val f = coverFile()
        if (f.exists()) {
            headerImageView.setImageBitmap(BitmapFactory.decodeFile(f.absolutePath))
        } else {
            headerImageView.setImageResource(R.mipmap.insert_image)
        }
        headerImageView.setOnClickListener {
            choosePictureLauncher.launch("image/*")
        }

//-------<底部导航栏区>-------

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            supportFragmentManager.popBackStack(
                null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
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
        binding.bottomNavigation.selectedItemId = R.id.navigation_todo


//-------<处理返回-销毁逻辑区>-------

        onBackPressedDispatcher.addCallback(this) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                return@addCallback
            }
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                return@addCallback
            }
            if (binding.bottomNavigation.selectedItemId != R.id.navigation_todo) {
                binding.bottomNavigation.selectedItemId = R.id.navigation_todo
                return@addCallback
            }
            val now = SystemClock.elapsedRealtime()
            if (now - lastBackPressedTime < 2000) {
                finish()
            } else {
                lastBackPressedTime = now
                Toast.makeText(this@MainActivity, "再按一次退出", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//    }

//-------<顶部抽屉区>-------

    override fun onDrawerMenuClicked() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

//-------<文件操作区>-------
    private fun copyUriToFile(uri: Uri, file: File): Boolean {
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file, false).use { output ->
                    input.copyTo(output)
                }
            } ?: return false
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
 //-------<权限申请区>-------
    //通知运行时权限 13

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
        when (requestCode) {
            REQ_POST_NOTIFICATIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "通知权限已授予", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "缺少通知权限，可能影响功能使用", Toast.LENGTH_LONG).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}
