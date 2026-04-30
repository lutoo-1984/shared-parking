package com.sharedparking.android.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sharedparking.android.R
import com.sharedparking.android.application.SharedParkingApp
import com.sharedparking.android.ui.auth.LoginActivity
import com.sharedparking.android.viewmodel.AuthViewModel

/**
 * 启动页面
 * 显示应用logo，检查登录状态，决定跳转到主页面还是登录页面
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    // 启动页显示时间（毫秒）
    private val SPLASH_DELAY = 1500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用splash主题
        setTheme(R.style.Theme_SharedParking_Splash)

        // 初始化ViewModel
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        // 延迟跳转
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginAndNavigate()
        }, SPLASH_DELAY)
    }

    /**
     * 检查登录状态并跳转到相应页面
     */
    private fun checkLoginAndNavigate() {
        if (authViewModel.isLoggedIn()) {
            // 已登录，跳转到主页面
            navigateToMain()
        } else {
            // 未登录，跳转到登录页面
            navigateToLogin()
        }
    }

    /**
     * 跳转到主页面
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * 跳转到登录页面
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * 防止用户按返回键跳过启动页
     */
    override fun onBackPressed() {
        // 禁用返回键
    }
}