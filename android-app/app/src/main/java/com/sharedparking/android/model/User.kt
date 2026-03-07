package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * 用户数据模型
 */
data class User(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("username")
    val username: String = "",

    @SerializedName("email")
    val email: String = "",

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("real_name")
    val realName: String? = null,

    @SerializedName("avatar_url")
    val avatarUrl: String? = null,

    @SerializedName("role")
    val role: String = "user",

    @SerializedName("is_verified")
    val isVerified: Boolean = false,

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("created_at")
    val createdAt: Date? = null,

    @SerializedName("last_login_at")
    val lastLoginAt: Date? = null
)

/**
 * 登录请求数据
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

/**
 * 注册请求数据
 */
data class RegisterRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("confirm_password")
    val confirmPassword: String? = null
)

/**
 * 登录响应数据
 */
data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: LoginData? = null,

    @SerializedName("error")
    val error: ApiError? = null
)

data class LoginData(
    @SerializedName("user")
    val user: User,

    @SerializedName("token")
    val token: String,

    @SerializedName("expires_in")
    val expiresIn: Int
)

/**
 * 注册响应数据
 */
data class RegisterResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: RegisterData? = null,

    @SerializedName("error")
    val error: ApiError? = null
)

data class RegisterData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("is_verified")
    val isVerified: Boolean,

    @SerializedName("message")
    val message: String? = null
)

/**
 * API错误响应
 */
data class ApiError(
    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String
)

/**
 * 基础API响应
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("error")
    val error: ApiError? = null
)