package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName

/**
 * 注册请求
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
    val confirmPassword: String,

    @SerializedName("verification_code")
    val verificationCode: String
)

/**
 * 注册响应
 */
data class RegisterResponse(
    @SerializedName("user")
    val user: User? = null,

    @SerializedName("token")
    val token: String? = null
)