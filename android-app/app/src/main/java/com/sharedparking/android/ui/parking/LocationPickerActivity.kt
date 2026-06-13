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
 * 使用高德地图SDK在地图上选择位置或获取当前位置
 *
 * 注意：使用前需要在 build.gradle 添加高德SDK依赖，并在AndroidManifest中配置Key
 *
 * build.gradle 依赖示例:
 *   implementation 'com.amap.api:map2d:latest.version'
 *   implementation 'com.amap.api:location:latest.version'
 *   implementation 'com.amap.api:search:latest.version'
 *
 * 最新版本号请参考 https://lbs.amap.com/api/android-sdk/guide/create-project/dev-dep
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

        setupToolbar()
        setupClickListeners()
        checkLocationPermission()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "选择位置"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnCurrentLocation.setOnClickListener {
            // 检查权限后获取当前位置
            if (hasLocationPermission()) {
                getCurrentLocation()
            } else {
                requestLocationPermission()
            }
        }

        binding.btnConfirm.setOnClickListener {
            val lat = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0)
            val lng = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)
            if (lat != 0.0 && lng != 0.0) {
                Toast.makeText(this, "已选择位置: $lat, $lng", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "请先在地图上选择位置", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationPermission() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initMapService()
            } else {
                Toast.makeText(this, "需要位置权限才能使用此功能", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 初始化高德地图服务
     *
     * 集成步骤：
     * 1. 在 build.gradle (app) 中添加依赖:
     *    implementation 'com.amap.api:map2d:10.0.0'
     *    implementation 'com.amap.api:location:6.4.3'
     *    implementation 'com.amap.api:search:9.7.0'
     *
     * 2. 在 AndroidManifest.xml 中添加 Key（已添加）
     *
     * 3. 初始化代码:
     *
     *    // 地图初始化
     *    val latLng = LatLng(39.909186, 116.397389)
     *    val mapOptions = AMapOptions()
     *        .mapType(AMap.MAP_TYPE_NORMAL)
     *        .camera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
     *    mapView = MapView(this, mapOptions)
     *    // 将 mapView 添加到布局中
     *
     *    // 定位初始化
     *    AMapLocationClient.setApiKey("你的高德Key")
     *    val locationClient = AMapLocationClient(this)
     *    val locationOption = AMapLocationClientOption().apply {
     *        locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
     *        isOnceLocation = true
     *    }
     *    locationClient.setLocationOption(locationOption)
     *    locationClient.setLocationListener { location ->
     *        runOnUiThread {
     *            val lat = location.latitude
     *            val lng = location.longitude
     *            // 更新地图标记
     *            if (lat != 0.0 && lng != 0.0) {
     *                markPosition(lat, lng)
     *            }
     *        }
     *    }
     *    locationClient.startLocation()
     */
    private fun initMapService() {
        // TODO: 集成高德地图SDK后在此初始化
        Toast.makeText(this, "请先集成高德地图SDK（参考代码注释）", Toast.LENGTH_LONG).show()
    }

    /**
     * 获取当前位置（高德定位）
     *
     * 集成示例：
     *   val locationClient = AMapLocationClient(this)
     *   locationClient.setLocationOption(AMapLocationClientOption().apply {
     *       locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
     *       isOnceLocation = true
     *   })
     *   locationClient.setLocationListener { location ->
     *       if (location.errorCode == 0) {
     *           markPosition(location.latitude, location.longitude)
     *           runOnUiThread {
     *               binding.etAddress.setText(location.address ?: "")
     *           }
     *       }
     *   }
     *   locationClient.startLocation()
     */
    private fun getCurrentLocation() {
        Toast.makeText(this, "请先集成高德定位SDK", Toast.LENGTH_SHORT).show()
    }

    /**
     * 在地图上标记位置
     */
    private fun markPosition(lat: Double, lng: Double) {
        /*
        // 高德SDK集成后的示例代码：
        val latLng = LatLng(lat, lng)
        if (marker == null) {
            marker = aMap.addMarker(MarkerOptions().position(latLng).title("选择的位置"))
        } else {
            marker.position = latLng
        }
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

        // 逆地理编码获取地址
        AMap.search(this, lat, lng) { address ->
            binding.etAddress.setText(address)
        }
        */
    }

    /**
     * 获取选中位置的地址（高德地理编码）
     */
    private fun getAddressFromLocation(lat: Double, lng: Double) {
        /*
        // 使用高德地理编码搜索 SDK
        val search = GeocodeSearch(this)
        search.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            override fun onRegeocodeSearched(result: RegeocodeResult, code: Int) {
                if (code == 1000 && result != null) {
                    val address = result.regeocodeAddress.formatAddress
                    runOnUiThread { binding.etAddress.setText(address) }
                }
            }
            override fun onGeocodeSearched(result: GeocodeResult, code: Int) {}
        })
        val query = RegeocodeQuery(LatLng(lat, lng), 200f, GeocodeSearch.AMAP)
        search.getFromLocationAsyn(query)
        */
    }

    override fun onResume() {
        super.onResume()
        // 高德地图 MapView.onResume()（集成后取消注释）
        // mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        // 高德地图 MapView.onPause()（集成后取消注释）
        // mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 高德地图 MapView.onDestroy()（集成后取消注释）
        // mapView?.onDestroy()
    }
}
