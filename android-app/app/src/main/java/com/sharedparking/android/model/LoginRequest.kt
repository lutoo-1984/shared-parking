package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName

/**
 * 登录请求
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("password")
    val password: String? = null,

    @SerializedName("verification_code")
    val verificationCode: String? = null,

    @SerializedName("login_type")
    val loginType: String = "password" // password 或 verification_code
)

/**
 * 登录响应
 */
data class LoginResponse(
    @SerializedName("user")
    val user: User? = null,

    @SerializedName("token")
    val token: String? = null
)