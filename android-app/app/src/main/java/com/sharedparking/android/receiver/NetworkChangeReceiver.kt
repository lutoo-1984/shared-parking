package com.sharedparking.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

/**
 * 网络变化接收器
 * 监听网络连接状态变化，在网络恢复时重新加载数据
 */
class NetworkChangeReceiver : BroadcastReceiver() {

    private var lastKnownStatus: NetworkStatus = NetworkStatus.UNKNOWN

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != ConnectivityManager.CONNECTIVITY_ACTION) return

        val currentStatus = getNetworkStatus(context)

        if (currentStatus != lastKnownStatus) {
            lastKnownStatus = currentStatus

            when (currentStatus) {
                NetworkStatus.CONNECTED -> {
                    Log.d(TAG, "网络已连接")
                    // 发送网络恢复广播，让Activity可以重新加载数据
                    val networkRestoredIntent = Intent(ACTION_NETWORK_RESTORED)
                    context?.sendBroadcast(networkRestoredIntent)
                }
                NetworkStatus.DISCONNECTED -> {
                    Log.w(TAG, "网络已断开")
                    val networkLostIntent = Intent(ACTION_NETWORK_LOST)
                    context?.sendBroadcast(networkLostIntent)
                }
                NetworkStatus.UNKNOWN -> {
                    // 未知状态
                }
            }
        }
    }

    /**
     * 获取当前网络状态
     */
    private fun getNetworkStatus(context: Context?): NetworkStatus {
        if (context == null) return NetworkStatus.UNKNOWN

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return NetworkStatus.UNKNOWN

        val network = connectivityManager.activeNetwork ?: return NetworkStatus.DISCONNECTED
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkStatus.DISCONNECTED

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkStatus.CONNECTED
            else -> NetworkStatus.DISCONNECTED
        }
    }

    /**
     * 检查网络是否可用（静态方法）
     */
    companion object {
        private const val TAG = "NetworkChangeReceiver"

        const val ACTION_NETWORK_RESTORED = "com.sharedparking.android.NETWORK_RESTORED"
        const val ACTION_NETWORK_LOST = "com.sharedparking.android.NETWORK_LOST"

        /**
         * 快速检查当前网络是否可用
         */
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false

            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }

    /**
     * 网络状态枚举
     */
    enum class NetworkStatus {
        CONNECTED, DISCONNECTED, UNKNOWN
    }
}
