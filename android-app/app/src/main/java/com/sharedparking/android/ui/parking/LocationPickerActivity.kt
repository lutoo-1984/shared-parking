package com.sharedparking.android.ui.parking

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.sharedparking.android.R
import com.sharedparking.android.databinding.ActivityLocationPickerBinding

/**
 * 位置选择器Activity
 * 用于在地图上选择位置或使用当前位置
 */
class LocationPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationPickerBinding
    private lateinit var baiduMap: BaiduMap
    private var locationClient: LocationClient? = null
    private var currentMarker: Marker? = null
    private var selectedLatLng: LatLng? = null

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

        // 初始化地图
        initMap()

        // 设置工具栏
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "选择位置"

        // 设置按钮事件
        setupButtons()

        // 检查位置权限
        checkLocationPermission()
    }

    /**
     * 初始化地图
     */
    private fun initMap() {
        baiduMap = binding.mapView.map

        // 设置地图配置
        val mapStatus = MapStatus.Builder()
            .zoom(15f) // 默认缩放级别
            .build()
        baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus))

        // 启用定位图层
        baiduMap.isMyLocationEnabled = true

        // 设置地图点击监听
        baiduMap.setOnMapClickListener { latLng ->
            updateMarker(latLng)
        }

        // 设置地图长按监听
        baiduMap.setOnMapLongClickListener { latLng ->
            updateMarker(latLng)
            Toast.makeText(this, "位置已选择", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 设置按钮事件
     */
    private fun setupButtons() {
        // 当前位置按钮
        binding.btnCurrentLocation.setOnClickListener {
            getCurrentLocation()
        }

        // 确认按钮
        binding.btnConfirm.setOnClickListener {
            confirmLocation()
        }

        // 取消按钮
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    /**
     * 检查位置权限
     */
    private fun checkLocationPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_PERMISSION)
        } else {
            initLocationClient()
        }
    }

    /**
     * 初始化定位客户端
     */
    private fun initLocationClient() {
        locationClient = LocationClient(applicationContext).apply {
            registerLocationListener(object : BDAbstractLocationListener() {
                override fun onReceiveLocation(location: BDLocation) {
                    if (location.locType == BDLocation.TypeGpsLocation ||
                        location.locType == BDLocation.TypeNetWorkLocation ||
                        location.locType == BDLocation.TypeOffLineLocation
                    ) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        updateMarker(latLng)
                        moveToLocation(latLng)

                        // 获取地址信息
                        val address = location.addrStr ?: "未知位置"
                        binding.tvSelectedAddress.text = address

                        // 停止定位，避免频繁更新
                        stop()
                    }
                }
            })
        }

        // 配置定位选项
        val option = LocationClientOption().apply {
            setIsNeedAddress(true) // 需要地址信息
            setIsNeedLocationDescribe(true) // 需要位置描述
            openGps() // 打开GPS
            setCoorType("bd09ll") // 设置坐标类型
            setScanSpan(1000) // 定位间隔
            setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy) // 高精度模式
        }

        locationClient?.locOption = option
    }

    /**
     * 获取当前位置
     */
    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient?.start()
        } else {
            Toast.makeText(this, "请先授予位置权限", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 更新标记位置
     */
    private fun updateMarker(latLng: LatLng) {
        selectedLatLng = latLng

        // 移除现有标记
        currentMarker?.remove()

        // 添加新标记
        val markerOptions = MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_pin))
            .zIndex(9)

        currentMarker = baiduMap.addOverlay(markerOptions) as Marker

        // 显示坐标信息
        binding.tvSelectedCoordinates.text = String.format(
            "%.6f, %.6f",
            latLng.latitude,
            latLng.longitude
        )
    }

    /**
     * 移动到指定位置
     */
    private fun moveToLocation(latLng: LatLng) {
        val mapStatus = MapStatus.Builder()
            .target(latLng)
            .zoom(17f)
            .build()
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus))
    }

    /**
     * 确认位置选择
     */
    private fun confirmLocation() {
        selectedLatLng?.let { latLng ->
            val intent = intent.apply {
                putExtra(EXTRA_LATITUDE, latLng.latitude)
                putExtra(EXTRA_LONGITUDE, latLng.longitude)
                putExtra(EXTRA_ADDRESS, binding.tvSelectedAddress.text.toString())
            }
            setResult(RESULT_OK, intent)
            finish()
        } ?: run {
            Toast.makeText(this, "请先选择位置", Toast.LENGTH_SHORT).show()
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
                    initLocationClient()
                } else {
                    Toast.makeText(this, "位置权限被拒绝，部分功能可能无法使用", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationClient?.stop()
        binding.mapView.onDestroy()
        baiduMap.isMyLocationEnabled = false
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}