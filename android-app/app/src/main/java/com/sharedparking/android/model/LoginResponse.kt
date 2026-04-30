package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName

/**
 * 登录响应数据
 */
data class LoginResponse(
    @SerializedName("token")
    val token: String = "",

    @SerializedName("user")
    val user: User = User()
)

/**
 * 注册响应数据
 */
data class RegisterResponse(
    @SerializedName("token")
    val token: String = "",

    @SerializedName("user")
    val user: User = User()
)