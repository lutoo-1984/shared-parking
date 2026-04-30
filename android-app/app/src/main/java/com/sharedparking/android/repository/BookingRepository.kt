package com.sharedparking.android.repository

import android.content.Context
import com.sharedparking.android.model.*
import com.sharedparking.android.network.ApiClientBuilder
import com.sharedparking.android.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.net.ConnectException
import java.net.UnknownHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException

/**
 * 预订仓库类
 * 处理预订相关的API调用
 */
class BookingRepository(private val context: Context) {

    private val apiService by lazy {
        ApiClientBuilder.getApiService()
    }

    // ===== 公开方法 =====

    /**
     * 创建预订
     */
    suspend fun createBooking(
        spotId: Int,
        vehiclePlateNumber: String,
        vehicleBrand: String,
        vehicleModel: String,
        vehicleColor: String?,
        startTime: Date,
        endTime: Date,
        notes: String?
    ): Resource<Booking> {
        return withContext(Dispatchers.IO) {
            try {
                val request = BookingRequest.create(
                    spotId = spotId,
                    vehiclePlateNumber = vehiclePlateNumber,
                    vehicleBrand = vehicleBrand,
                    vehicleModel = vehicleModel,
                    vehicleColor = vehicleColor,
                    startTime = startTime,
                    endTime = endTime,
                    notes = notes
                )

                val response = apiService.createBooking(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val booking = body.data as Booking
                        Resource.Success(booking)
                    } else {
                        Resource.Error(body?.message ?: "创建预订失败")
                    }
                } else {
                    Resource.Error("网络请求失败: ${response.code()}")
                }
            } catch (e: Exception) {
                if (DemoMode.isEnabled && e.isNetworkError()) {
                    Resource.Success(DemoMode.demoBookings.first())
                } else {
                    Resource.Error(e.message ?: "网络错误")
                }
            }
        }
    }

    /**
     * 获取预订详情
     */
    suspend fun getBooking(bookingId: Int): Resource<Booking> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBooking(bookingId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val booking = body.data as Booking
                        Resource.Success(booking)
                    } else {
                        Resource.Error(body?.message ?: "获取预订详情失败")
                    }
                } else {
                    Resource.Error("网络请求失败: ${response.code()}")
                }
            } catch (e: Exception) {
                if (DemoMode.isEnabled && e.isNetworkError()) {
                    val booking = DemoMode.demoBookings.find { it.id == bookingId }
                    if (booking != null) Resource.Success(booking)
                    else Resource.Error("预订不存在")
                } else {
                    Resource.Error(e.message ?: "网络错误")
                }
            }
        }
    }

    /**
     * 获取我的预订列表
     */
    suspend fun getMyBookings(page: Int = 1, limit: Int = 20): Resource<BookingListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyBookings(page, limit)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val bookingList = body.data as BookingListResponse
                        Resource.Success(bookingList)
                    } else {
                        Resource.Error(body?.message ?: "获取预订列表失败")
                    }
                } else {
                    Resource.Error("网络请求失败: ${response.code()}")
                }
            } catch (e: Exception) {
                if (DemoMode.isEnabled && e.isNetworkError()) {
                    val bookingList = BookingListResponse(
                        bookings = DemoMode.demoBookings,
                        total = DemoMode.demoBookings.size,
                        page = page,
                        limit = limit,
                        totalPages = 1
                    )
                    Resource.Success(bookingList)
                } else {
                    Resource.Error(e.message ?: "网络错误")
                }
            }
        }
    }

    /**
     * 取消预订
     */
    suspend fun cancelBooking(bookingId: Int): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.cancelBooking(bookingId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Resource.Success(Unit)
                    } else {
                        Resource.Error(body?.message ?: "取消预订失败")
                    }
                } else {
                    Resource.Error("网络请求失败: ${response.code()}")
                }
            } catch (e: Exception) {
                if (DemoMode.isEnabled && e.isNetworkError()) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(e.message ?: "网络错误")
                }
            }
        }
    }

    /**
     * 检查车位可用性
     */
    suspend fun checkAvailability(
        spotId: Int,
        startTime: Date,
        endTime: Date
    ): Resource<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val isoFormatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
                val response = apiService.checkAvailability(
                    id = spotId,
                    startTime = isoFormatter.format(startTime),
                    endTime = isoFormatter.format(endTime)
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val availabilityResponse = body.data as AvailabilityResponse
                        Resource.Success(availabilityResponse.available)
                    } else {
                        Resource.Error(body?.message ?: "检查可用性失败")
                    }
                } else {
                    Resource.Error("网络请求失败: ${response.code()}")
                }
            } catch (e: Exception) {
                if (DemoMode.isEnabled && e.isNetworkError()) {
                    Resource.Success(true)
                } else {
                    Resource.Error(e.message ?: "网络错误")
                }
            }
        }
    }

    /**
     * 计算价格
     */
    fun calculatePrice(startTime: Date, endTime: Date, pricePerHour: Double): Double {
        val durationHours = (endTime.time - startTime.time) / (1000.0 * 60 * 60)
        return durationHours * pricePerHour
    }

    /**
     * 计算时长（小时）
     */
    fun calculateDurationHours(startTime: Date, endTime: Date): Double {
        return (endTime.time - startTime.time) / (1000.0 * 60 * 60)
    }

    /**
     * 验证车牌号格式
     */
    fun validatePlateNumber(plateNumber: String): Boolean {
        // 简单的车牌号验证规则（中国车牌号）
        val pattern = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{4,5}[A-Z0-9挂学警港澳]$"
        return plateNumber.matches(Regex(pattern))
    }

    /**
     * 验证时间范围
     */
    fun validateTimeRange(startTime: Date, endTime: Date): Pair<Boolean, String?> {
        val now = Date()

        // 开始时间不能早于当前时间
        if (startTime.before(now)) {
            return Pair(false, "开始时间不能早于当前时间")
        }

        // 结束时间必须晚于开始时间
        if (!endTime.after(startTime)) {
            return Pair(false, "结束时间必须晚于开始时间")
        }

        // 最小预订时长：1小时
        val minDurationHours = 1.0
        val durationHours = calculateDurationHours(startTime, endTime)
        if (durationHours < minDurationHours) {
            return Pair(false, "预订时长至少1小时")
        }

        // 最大预订时长：30天
        val maxDurationHours = 30 * 24.0
        if (durationHours > maxDurationHours) {
            return Pair(false, "预订时长不能超过30天")
        }

        return Pair(true, null)
    }
}

private fun Exception.isNetworkError(): Boolean {
    return this is ConnectException ||
            this is UnknownHostException ||
            this is SocketException ||
            this is SocketTimeoutException ||
            this is SSLException
}