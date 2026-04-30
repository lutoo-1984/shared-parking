package com.sharedparking.android.ui.parking

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sharedparking.android.R
import com.sharedparking.android.databinding.ActivityLocationPickerBinding

/**
 * 位置选择器Activity
 * 用于在地图上选择位置或使用当前位置
 * 注意：百度地图SDK暂时注释，需要下载BaiduLBS_Android.jar放入libs目录
 */
class LocationPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationPickerBinding

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_ADDRESS = "address"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: 百度地图SDK暂时注释
        // initMap()

        // 设置工具栏
        setupToolbar()

        // 设置按钮点击事件
        setupClickListeners()

        // 检查位置权限
        checkLocationPermission()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("选择位置")
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnCurrentLocation.setOnClickListener {
            // TODO: 获取当前位置
            Toast.makeText(this, "获取当前位置功能暂不可用", Toast.LENGTH_SHORT).show()
        }

        binding.btnConfirm.setOnClickListener {
            // TODO: 确认选择的位置
            Toast.makeText(this, "确认位置功能暂不可用", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // 已经有权限
            // TODO: 初始化位置服务
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限已授予
                    // TODO: 初始化位置服务
                } else {
                    Toast.makeText(this, "需要位置权限才能使用此功能", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // TODO: 以下方法需要百度地图SDK
    /*
    private fun initMap() {
        // 百度地图初始化代码
    }

    private fun initLocationService() {
        // 位置服务初始化代码
    }

    private fun getCurrentLocation() {
        // 获取当前位置代码
    }

    private fun updateMapCenter(lat: Double, lng: Double) {
        // 更新地图中心代码
    }

    private fun addMarker(lat: Double, lng: Double) {
        // 添加标记代码
    }

    override fun onResume() {
        super.onResume()
        // 百度地图onResume
    }

    override fun onPause() {
        super.onPause()
        // 百度地图onPause
    }

    override fun onDestroy() {
        super.onDestroy()
        // 百度地图onDestroy
    }
    */
}