package com.yywspace.simplefilemanager

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {
    companion object {
        var READ_EXTERNAL_STORAGE_CODE = 0
        var WRITE_EXTERNAL_STORAGE_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.file_list_tool_bar)
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0)
        setSupportActionBar(toolbar)
        // toolbar绑定NavController
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        // 透明状态栏
        val option: Int = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.decorView.systemUiVisibility = option
        window.statusBarColor = Color.TRANSPARENT

        // 获取权限
        val permissions = mapOf(
            READ_EXTERNAL_STORAGE_CODE to android.Manifest.permission.READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE_CODE to android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(
                    this, it.value
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(it.value),
                    it.key
                )
            }
        }
    }

    // only work in activity onCreate
    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}
