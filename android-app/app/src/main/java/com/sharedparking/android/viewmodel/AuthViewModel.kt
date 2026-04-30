package com.sharedparking.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sharedparking.android.model.User
import com.sharedparking.android.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * 认证相关的ViewModel
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    // 登录状态
    private val _loginState = MutableLiveData<AuthState>()
    val loginState: LiveData<AuthState> = _loginState

    // 注册状态
    private val _registerState = MutableLiveData<AuthState>()
    val registerState: LiveData<AuthState> = _registerState

    // 当前用户信息
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    // 验证码发送状态
    private val _captchaState = MutableLiveData<CaptchaState>()
    val captchaState: LiveData<CaptchaState> = _captchaState

    // 初始化：检查本地是否有登录状态
    init {
        // 初始化认证状态
        authRepository.initAuthState()

        // 如果本地有token，尝试获取用户信息
        if (authRepository.isLoggedIn()) {
            viewModelScope.launch {
                fetchCurrentUser()
            }
        }
    }

    /**
     * 演示模式登录
     */
    fun demoLogin() {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            try {
                val result = authRepository.demoLogin()
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    _currentUser.postValue(user)
                    _loginState.value = AuthState.Success(user)
                } else {
                    _loginState.value = AuthState.Error("演示模式启动失败")
                }
            } catch (e: Exception) {
                _loginState.value = AuthState.Error(e.message ?: "演示模式启动异常")
            }
        }
    }

    /**
     * 用户登录
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            try {
                val result = authRepository.login(email, password)
                if (result.isSuccess) {
                    _loginState.value = AuthState.Success(result.getOrNull())
                    // 登录成功后获取用户信息
                    fetchCurrentUser()
                } else {
                    _loginState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "登录失败")
                }
            } catch (e: Exception) {
                _loginState.value = AuthState.Error(e.message ?: "登录异常")
            }
        }
    }

    /**
     * 用户注册
     */
    fun register(username: String, email: String, phone: String, password: String, confirmPassword: String, verificationCode: String) {
        viewModelScope.launch {
            _registerState.value = AuthState.Loading
            try {
                val result = authRepository.register(username, email, phone, password, confirmPassword, verificationCode)
                if (result.isSuccess) {
                    _registerState.value = AuthState.Success(result.getOrNull())
                } else {
                    _registerState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "注册失败")
                }
            } catch (e: Exception) {
                _registerState.value = AuthState.Error(e.message ?: "注册异常")
            }
        }
    }

    /**
     * 发送验证码
     */
    fun sendCaptcha(phone: String, type: String = "register") {
        viewModelScope.launch {
            _captchaState.value = CaptchaState.Loading
            try {
                val result = authRepository.sendCaptcha(phone, type)
                if (result.isSuccess) {
                    _captchaState.value = CaptchaState.Success
                } else {
                    _captchaState.value = CaptchaState.Error(result.exceptionOrNull()?.message ?: "发送验证码失败")
                }
            } catch (e: Exception) {
                _captchaState.value = CaptchaState.Error(e.message ?: "发送验证码异常")
            }
        }
    }

    /**
     * 验证验证码
     */
    suspend fun verifyCode(phone: String, code: String, type: String = "register"): Boolean {
        return try {
            val result = authRepository.verifyCode(phone, code, type)
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取当前用户信息
     */
    private suspend fun fetchCurrentUser() {
        try {
            val result = authRepository.getCurrentUser()
            if (result.isSuccess) {
                _currentUser.postValue(result.getOrNull())
            } else {
                // 获取失败，清除登录状态
                logout()
            }
        } catch (e: Exception) {
            // 异常，清除登录状态
            logout()
        }
    }

    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }

    /**
     * 获取当前用户ID
     */
    fun getCurrentUserId(): Int? {
        return authRepository.getCurrentUserId()
    }

    /**
     * 获取当前用户邮箱
     */
    fun getCurrentUserEmail(): String? {
        return authRepository.getCurrentUserEmail()
    }

    /**
     * 获取当前用户名
     */
    fun getCurrentUserName(): String? {
        return authRepository.getCurrentUserName()
    }

    /**
     * 退出登录
     */
    fun logout() {
        authRepository.logout()
        _currentUser.postValue(null)
        _loginState.postValue(AuthState.Idle)
    }

    /**
     * 更新用户资料
     */
    fun updateUserProfile(updates: Map<String, Any>) {
        viewModelScope.launch {
            try {
                val result = authRepository.updateUserProfile(updates)
                if (result.isSuccess) {
                    // 更新成功，重新获取用户信息
                    fetchCurrentUser()
                }
            } catch (e: Exception) {
                // 忽略错误
            }
        }
    }

    /**
     * 重置登录状态
     */
    fun resetLoginState() {
        _loginState.value = AuthState.Idle
    }

    /**
     * 重置注册状态
     */
    fun resetRegisterState() {
        _registerState.value = AuthState.Idle
    }

    /**
     * 重置验证码状态
     */
    fun resetCaptchaState() {
        _captchaState.value = CaptchaState.Idle
    }
}

/**
 * 认证状态密封类
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User?) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * 验证码状态密封类
 */
sealed class CaptchaState {
    object Idle : CaptchaState()
    object Loading : CaptchaState()
    object Success : CaptchaState()
    data class Error(val message: String) : CaptchaState()
}