package com.sharedparking.android.network

import com.sharedparking.android.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API服务接口定义
 */
interface ApiService {

    // ===== 认证相关接口 =====

    /**
     * 用户注册
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<RegisterResponse>>

    /**
     * 用户登录
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    /**
     * 发送验证码
     */
    @POST("auth/send-captcha")
    suspend fun sendCaptcha(@Body request: Map<String, String>): Response<ApiResponse<Map<String, Any>>>

    /**
     * 验证验证码
     */
    @POST("auth/verify-code")
    suspend fun verifyCode(@Body request: Map<String, String>): Response<ApiResponse<Any>>

    /**
     * 获取当前用户信息
     */
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<User>>

    // ===== 用户相关接口 =====

    /**
     * 获取用户资料
     */
    @GET("users/profile")
    suspend fun getUserProfile(): Response<ApiResponse<User>>

    /**
     * 更新用户资料
     */
    @PUT("users/profile")
    suspend fun updateUserProfile(@Body request: Map<String, Any>): Response<ApiResponse<Any>>

    // ===== 停车位相关接口 =====

    /**
     * 搜索停车位
     */
    @GET("parking/spots")
    suspend fun searchParkingSpots(@QueryMap filters: Map<String, String>): Response<ApiResponse<ParkingSearchResponse>>

    /**
     * 获取停车位详情
     */
    @GET("parking/spots/{id}")
    suspend fun getParkingSpot(@Path("id") id: Int): Response<ApiResponse<ParkingSpot>>

    /**
     * 创建停车位
     */
    @POST("parking/spots")
    suspend fun createParkingSpot(@Body request: CreateParkingSpotRequest): Response<ApiResponse<ParkingSpot>>

    /**
     * 更新停车位
     */
    @PUT("parking/spots/{id}")
    suspend fun updateParkingSpot(
        @Path("id") id: Int,
        @Body request: CreateParkingSpotRequest
    ): Response<ApiResponse<ParkingSpot>>

    /**
     * 删除停车位
     */
    @DELETE("parking/spots/{id}")
    suspend fun deleteParkingSpot(@Path("id") id: Int): Response<ApiResponse<Any>>

    /**
     * 获取我的车位
     */
    @GET("parking/my")
    suspend fun getMyParkingSpots(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<ParkingSearchResponse>>

    /**
     * 检查车位可用性
     */
    @GET("parking/availability/{id}")
    suspend fun checkAvailability(
        @Path("id") id: Int,
        @Query("start_time") startTime: String,
        @Query("end_time") endTime: String
    ): Response<ApiResponse<AvailabilityResponse>>

    // ===== 收藏相关接口 =====

    /**
     * 添加收藏
     */
    @POST("favorites/{spot_id}")
    suspend fun addFavorite(@Path("spot_id") spotId: Int): Response<ApiResponse<Any>>

    /**
     * 移除收藏
     */
    @DELETE("favorites/{spot_id}")
    suspend fun removeFavorite(@Path("spot_id") spotId: Int): Response<ApiResponse<Any>>

    /**
     * 获取收藏列表
     */
    @GET("favorites")
    suspend fun getFavorites(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<ParkingSearchResponse>>

    // ===== 预订相关接口 =====

    /**
     * 创建预订
     */
    @POST("bookings")
    suspend fun createBooking(@Body request: BookingRequest): Response<ApiResponse<Booking>>

    /**
     * 获取预订详情
     */
    @GET("bookings/{id}")
    suspend fun getBooking(@Path("id") id: Int): Response<ApiResponse<Booking>>

    /**
     * 获取我的预订
     */
    @GET("bookings")
    suspend fun getMyBookings(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<BookingListResponse>>

    /**
     * 取消预订
     */
    @PUT("bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: Int): Response<ApiResponse<Any>>

    // ===== 支付相关接口 =====

    /**
     * 创建支付
     */
    @POST("payments/create")
    suspend fun createPayment(@Body request: PaymentRequest): Response<ApiResponse<Payment>>

    /**
     * 获取支付详情
     */
    @GET("payments/{id}")
    suspend fun getPayment(@Path("id") id: Int): Response<ApiResponse<Payment>>

    /**
     * 获取预订的支付记录
     */
    @GET("payments/booking/{booking_id}")
    suspend fun getBookingPayment(@Path("booking_id") bookingId: Int): Response<ApiResponse<Payment>>

    /**
     * 获取我的支付记录
     */
    @GET("payments")
    suspend fun getMyPayments(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<PaymentListResponse>>

    /**
     * 退款申请
     */
    @POST("payments/{id}/refund")
    suspend fun requestRefund(
        @Path("id") id: Int,
        @Body request: RefundRequest
    ): Response<ApiResponse<Payment>>

    // ===== 消息相关接口 =====

    /**
     * 获取收件箱
     */
    @GET("messages")
    suspend fun getInbox(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<MessageListResponse>>

    /**
     * 获取与某用户的对话
     */
    @GET("messages/conversation/{user_id}")
    suspend fun getConversation(
        @Path("user_id") otherUserId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<MessageListResponse>>

    /**
     * 发送消息
     */
    @POST("messages")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<ApiResponse<com.sharedparking.android.model.Message>>

    /**
     * 标记消息为已读
     */
    @PUT("messages/{id}/read")
    suspend fun markMessageRead(@Path("id") id: Int): Response<ApiResponse<Any>>

    /**
     * 获取未读消息数
     */
    @GET("messages/unread")
    suspend fun getUnreadCount(): Response<ApiResponse<UnreadCountResponse>>

    /**
     * 删除消息
     */
    @DELETE("messages/{id}")
    suspend fun deleteMessage(@Path("id") id: Int): Response<ApiResponse<Any>>

    // ===== 评价相关接口 =====

    /**
     * 创建评价
     */
    @POST("reviews")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<ApiResponse<com.sharedparking.android.model.Review>>

    /**
     * 获取车位评价
     */
    @GET("reviews/spot/{spot_id}")
    suspend fun getSpotReviews(
        @Path("spot_id") spotId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<ReviewListResponse>>
}

/**
 * API客户端配置
 */
object ApiClient {
    // API基础URL - 需要根据实际环境配置
    const val BASE_URL = "http://192.168.0.107:8080/api/" // 电脑局域网IP，手机需与电脑在同一WiFi

    // 从SharedPreferences或SecureStorage获取token
    var authToken: String? = null
        private set

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun clearAuthToken() {
        authToken = null
    }

    fun hasToken(): Boolean {
        return !authToken.isNullOrEmpty()
    }

    fun getAuthHeader(): String? {
        return authToken?.let { "Bearer $it" }
    }
}