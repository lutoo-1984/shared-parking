package com.sharedparking.android.application

import android.app.Application
import com.sharedparking.android.repository.AuthRepository

/**
 * 应用程序类
 * 负责全局初始化和配置
 */
class SharedParkingApp : Application() {

    companion object {
        // 全局Application实例
        private lateinit var instance: SharedParkingApp

        fun getInstance(): SharedParkingApp = instance
    }

    // ViewModel工厂
    lateinit var viewModelFactory: ViewModelFactory

    // 认证仓库（可以在需要的地方注入）
    lateinit var authRepository: AuthRepository

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化仓库
        authRepository = AuthRepository(this)

        // 初始化ViewModel工厂
        viewModelFactory = ViewModelFactory(authRepository)

        // 初始化认证状态
        authRepository.initAuthState()

        // 可以在这里初始化其他组件，如Crashlytics、Analytics等
    }

    /**
     * 简单的ViewModel工厂
     * 在实际应用中应该使用更完善的依赖注入框架（如Hilt）
     */
    class ViewModelFactory(private val authRepository: AuthRepository) {
        // 这里可以创建需要的ViewModel实例
        // 在实际应用中，应该使用更完善的依赖注入
    }
}