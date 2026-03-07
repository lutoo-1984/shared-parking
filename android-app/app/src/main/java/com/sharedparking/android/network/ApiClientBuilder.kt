package com.sharedparking.android.network

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit客户端构建器
 */
object ApiClientBuilder {

    // 单例Retrofit实例
    private var retrofit: Retrofit? = null

    // API服务实例
    private var apiService: ApiService? = null

    /**
     * 获取API服务实例
     */
    fun getApiService(): ApiService {
        if (apiService == null) {
            apiService = getRetrofit().create(ApiService::class.java)
        }
        return apiService!!
    }

    /**
     * 获取Retrofit实例
     */
    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .client(createOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(createGson()))
                .build()
        }
        return retrofit!!
    }

    /**
     * 创建OkHttpClient
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 开发环境记录完整日志
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()

            // 添加认证头
            val requestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")

            // 如果存在token，添加到请求头
            ApiClient.getAuthHeader()?.let { token ->
                requestBuilder.header("Authorization", token)
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(createErrorInterceptor())
            .build()
    }

    /**
     * 创建错误拦截器，处理HTTP错误
     */
    private fun createErrorInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)

            // 检查响应状态码
            if (!response.isSuccessful) {
                // 可以根据状态码进行特定处理
                when (response.code) {
                    401 -> {
                        // 未授权，清除token
                        ApiClient.clearAuthToken()
                        // 可以在这里触发重新登录流程
                    }
                    403 -> {
                        // 禁止访问
                    }
                    404 -> {
                        // 资源未找到
                    }
                    500 -> {
                        // 服务器内部错误
                    }
                }
            }

            response
        }
    }

    /**
     * 创建Gson实例，配置日期格式等
     */
    private fun createGson() = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .setDateFormat("yyyy-MM-dd")
        .create()

    /**
     * 清理缓存，用于重新配置（如切换环境）
     */
    fun clearCache() {
        retrofit = null
        apiService = null
    }

    /**
     * 更新基础URL（用于切换环境）
     */
    fun updateBaseUrl(newBaseUrl: String) {
        clearCache()
        // 这里可以重新配置ApiClient.BASE_URL
        // 注意：实际应用中应该通过配置管理来设置BASE_URL
    }
}