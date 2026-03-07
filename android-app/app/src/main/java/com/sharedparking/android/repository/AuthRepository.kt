package com.sharedparking.android.repository

import android.content.Context
import android.content.SharedPreferences
import com.sharedparking.android.model.LoginRequest
import com.sharedparking.android.model.RegisterRequest
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
     * 用户登录
     */
    suspend fun login(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email = email, password = password)
                val response = apiService.login(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        // 保存token和用户信息
                        saveAuthData(body.data.token, body.data.user)
                        Result.success(body.data.user)
                    } else {
                        val error = body?.error ?: throw Exception("登录失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
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
        password: String
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(
                    username = username,
                    email = email,
                    phone = phone,
                    password = password
                )
                val response = apiService.register(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        // 注册成功，返回用户信息（但未登录）
                        val user = User(
                            id = body.data.id,
                            username = body.data.username,
                            email = body.data.email,
                            phone = body.data.phone,
                            isVerified = body.data.isVerified
                        )
                        Result.success(user)
                    } else {
                        val error = body?.error ?: throw Exception("注册失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
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
                Result.failure(e)
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
                Result.failure(e)
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
                Result.failure(e)
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
                Result.failure(e)
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
}