package com.sharedparking.android.utils

/**
 * 通用的资源状态封装类
 * 用于处理加载、成功、错误等状态
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Idle<T> : Resource<T>()

    override fun toString(): String {
        return when (this) {
            is Success -> "Success[data=$data]"
            is Error -> "Error[message=$message]"
            is Loading -> "Loading"
            is Idle -> "Idle"
        }
    }
}