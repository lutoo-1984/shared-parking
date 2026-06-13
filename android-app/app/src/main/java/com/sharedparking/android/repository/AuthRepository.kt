package com.sharedparking.android.repository

import android.content.Context
import android.content.SharedPreferences
import com.sharedparking.android.model.ApiResponse
import com.sharedparking.android.model.DemoMode
import com.sharedparking.android.model.LoginRequest
import com.sharedparking.android.model.LoginResponse
import com.sharedparking.android.model.RegisterRequest
import com.sharedparking.android.model.RegisterResponse
import com.sharedparking.android.model.User
import com.sharedparking.android.network.ApiClient
import com.sharedparking.android.network.ApiClientBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 认证仓库类
 * 处理用户登录、注册、token管理等
 */
class AuthRepository(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "shared_parking_auth"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val apiService by lazy {
        ApiClientBuilder.getApiService()
    }

    // ===== 公开方法 =====

    /**
     * 演示模式登录（不依赖服务端）
     */
    suspend fun demoLogin(): Result<User> {
        return withContext(Dispatchers.IO) {
            DemoMode.isEnabled = true
            val user = DemoMode.demoUser
            saveAuthData("demo_token_demo_mode", user)
            Result.success(user)
        }
    }

    /**
     * 用户登录
     */
    suspend fun login(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email = email, password = password)
                val response = apiService.login(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    val loginData = body?.data
                    if (body?.success == true && loginData != null && loginData.token.isNotEmpty() && loginData.user.id > 0) {
                        // 保存token和用户信息
                        saveAuthData(loginData.token, loginData.user)
                        Result.success(loginData.user)
                    } else {
                        val errMsg = body?.error?.message ?: "登录失败: 响应数据无效"
                        Result.failure(Exception(errMsg))
                    }
                } else {
                    val errMsg = parseErrorBody(response)
                    Result.failure(Exception(errMsg))
                }
            } catch (e: Exception) {
                if (DemoMode.isEnabled) {
                    Result.success(DemoMode.demoUser)
                } else {
                    Result.failure(e)
                }
            }
        }
    }

    /**
     * 用户注册
     */
    suspend fun register(
        username: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        verificationCode: String
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(
                    username = username,
                    email = email,
                    phone = phone,
                    password = password,
                    confirmPassword = confirmPassword,
                    verificationCode = verificationCode
                )
                val response = apiService.register(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    val registerData = body?.data
                    if (body?.success == true && registerData != null && registerData.user.id > 0) {
                        // 注册成功，自动登录，保存token和用户信息
                        saveAuthData(registerData.token, registerData.user)
                        Result.success(registerData.user)
                    } else {
                        val errMsg = body?.error?.message ?: "注册失败: 响应数据无效"
                        Result.failure(Exception(errMsg))
                    }
                } else {
                    val errMsg = parseErrorBody(response)
                    Result.failure(Exception(errMsg))
                }
            } catch (e: Exception) {
                if (DemoMode.isEnabled) {
                    Result.success(DemoMode.demoUser)
                } else {
                    Result.failure(e)
                }
            }
        }
    }

    /**
     * 发送验证码
     */
    suspend fun sendCaptcha(phone: String, type: String = "register"): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val request = mapOf(
                    "phone" to phone,
                    "type" to type
                )
                val response = apiService.sendCaptcha(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.success(true)
                    } else {
                        val error = body?.error ?: throw Exception("发送验证码失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                if (DemoMode.isEnabled) {
                    Result.success(true)
                } else {
                    Result.failure(e)
                }
            }
        }
    }

    /**
     * 验证验证码
     */
    suspend fun verifyCode(
        phone: String,
        code: String,
        type: String = "register"
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val request = mapOf(
                    "phone" to phone,
                    "code" to code,
                    "type" to type
                )
                val response = apiService.verifyCode(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.success(true)
                    } else {
                        val error = body?.error ?: throw Exception("验证码验证失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                if (DemoMode.isEnabled) {
                    Result.success(true)
                } else {
                    Result.failure(e)
                }
            }
        }
    }

    /**
     * 获取当前用户信息
     */
    suspend fun getCurrentUser(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentUser()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.success(body.data)
                    } else {
                        // 如果获取失败，可能是token过期，清除本地存储
                        if (body?.error?.code == 401) {
                            clearAuthData()
                        }
                        val error = body?.error ?: throw Exception("获取用户信息失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    if (response.code() == 401) {
                        clearAuthData()
                    }
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                if (DemoMode.isEnabled) {
                    Result.success(DemoMode.demoUser)
                } else {
                    Result.failure(e)
                }
            }
        }
    }

    /**
     * 更新用户资料
     */
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateUserProfile(updates)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.success(true)
                    } else {
                        val error = body?.error ?: throw Exception("更新用户资料失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                if (DemoMode.isEnabled) {
                    Result.success(true)
                } else {
                    Result.failure(e)
                }
            }
        }
    }

    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }

    /**
     * 获取当前登录用户ID
     */
    fun getCurrentUserId(): Int? {
        return prefs.getInt(KEY_USER_ID, -1).takeIf { it != -1 }
    }

    /**
     * 获取当前登录用户邮箱
     */
    fun getCurrentUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    /**
     * 获取当前登录用户名
     */
    fun getCurrentUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    /**
     * 退出登录
     */
    fun logout() {
        DemoMode.reset()
        clearAuthData()
    }

    // ===== 私有方法 =====

    /**
     * 保存认证数据
     */
    private fun saveAuthData(token: String, user: User) {
        // 保存到内存
        ApiClient.setAuthToken(token)

        // 保存到SharedPreferences
        prefs.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            putInt(KEY_USER_ID, user.id)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_NAME, user.username)
            apply()
        }
    }

    /**
     * 清除认证数据
     */
    private fun clearAuthData() {
        // 清除内存
        ApiClient.clearAuthToken()

        // 清除SharedPreferences
        prefs.edit().clear().apply()
    }

    /**
     * 获取认证token
     */
    private fun getAuthToken(): String? {
        // 优先从内存获取
        if (ApiClient.hasToken()) {
            return ApiClient.getAuthHeader()
        }

        // 从SharedPreferences获取
        val token = prefs.getString(KEY_AUTH_TOKEN, null)
        if (!token.isNullOrEmpty()) {
            ApiClient.setAuthToken(token)
            return token
        }

        return null
    }

    /**
     * 初始化认证状态
     * 应用启动时调用
     */
    fun initAuthState() {
        // 从SharedPreferences恢复token
        val token = prefs.getString(KEY_AUTH_TOKEN, null)
        if (!token.isNullOrEmpty()) {
            ApiClient.setAuthToken(token)
        }
    }

    /**
     * 解析HTTP错误响应体，提取错误信息
     */
    private fun parseErrorBody(response: retrofit2.Response<*>): String {
        return try {
            val errBody = response.errorBody()?.string()
            if (!errBody.isNullOrEmpty()) {
                val errJson = com.google.gson.Gson().fromJson(errBody, ApiResponse::class.java)
                errJson.error?.message ?: "网络请求失败: ${response.code()}"
            } else {
                "网络请求失败: ${response.code()}"
            }
        } catch (e: Exception) {
            "网络请求失败: ${response.code()}"
        }
    }
}