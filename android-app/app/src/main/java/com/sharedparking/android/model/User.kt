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

