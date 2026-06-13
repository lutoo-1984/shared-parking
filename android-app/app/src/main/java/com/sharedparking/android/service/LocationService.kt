package com.sharedparking.android.service

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.location.LocationManagerCompat
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 位置服务
 * 提供设备位置跟踪功能
 */
class LocationService : Service() {

    private val binder = LocalBinder()
    private var currentLocation: Location? = null
    private val listeners = CopyOnWriteArrayList<LocationListener>()

    // 最后已知位置缓存
    private var lastLatitude: Double = 0.0
    private var lastLongitude: Double = 0.0

    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "位置服务启动")
        startLocationTracking()
        return START_STICKY
    }

    private fun startLocationTracking() {
        // 使用Android的LocationManager获取位置更新
        // 在实际应用中，这里应该使用FusedLocationProviderClient
        try {
            val locationManager = getSystemService(LOCATION_SERVICE) as android.location.LocationManager

            // 检查GPS是否可用
            val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                    android.location.LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_MS,
                    MIN_DISTANCE_M,
                    locationListener
                )
            }

            if (isGpsEnabled) {
                locationManager.requestLocationUpdates(
                    android.location.LocationManager.GPS_PROVIDER,
                    MIN_TIME_MS,
                    MIN_DISTANCE_M,
                    locationListener
                )
            }

            // 获取最后已知位置
            val lastKnownLocation = locationManager.getLastKnownLocation(
                android.location.LocationManager.GPS_PROVIDER
            ) ?: locationManager.getLastKnownLocation(
                android.location.LocationManager.NETWORK_PROVIDER
            )

            if (lastKnownLocation != null) {
                updateLocation(lastKnownLocation)
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "位置权限不足: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "位置服务出错: ${e.message}")
        }
    }

    private val locationListener = android.location.LocationListener { location ->
        updateLocation(location)
    }

    private fun updateLocation(location: Location) {
        currentLocation = location
        lastLatitude = location.latitude
        lastLongitude = location.longitude

        // 通知所有监听器
        for (listener in listeners) {
            listener.onLocationChanged(location.latitude, location.longitude)
        }
    }

    /**
     * 获取最后已知纬度
     */
    fun getLastLatitude(): Double = lastLatitude

    /**
     * 获取最后已知经度
     */
    fun getLastLongitude(): Double = lastLongitude

    /**
     * 注册位置监听器
     */
    fun addLocationListener(listener: LocationListener) {
        listeners.add(listener)
    }

    /**
     * 移除位置监听器
     */
    fun removeLocationListener(listener: LocationListener) {
        listeners.remove(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            val locationManager = getSystemService(LOCATION_SERVICE) as android.location.LocationManager
            locationManager.removeUpdates(locationListener)
        } catch (e: Exception) {
            // 忽略移除监听器时的错误
        }
        listeners.clear()
        Log.d(TAG, "位置服务停止")
    }

    /**
     * 位置变化监听接口
     */
    interface LocationListener {
        fun onLocationChanged(latitude: Double, longitude: Double)
    }

    companion object {
        private const val TAG = "LocationService"
        private const val MIN_TIME_MS = 5000L // 5秒
        private const val MIN_DISTANCE_M = 10f // 10米
    }
}
