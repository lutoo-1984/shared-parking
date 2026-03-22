package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName

/**
 * API响应通用格式
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("error")
    val error: ApiError? = null
)

/**
 * API错误信息
 */
data class ApiError(
    @SerializedName("code")
    val code: Int = 0,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("details")
    val details: Map<String, Any>? = null
)