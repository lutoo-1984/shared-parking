package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * 停车位数据模型
 */
data class ParkingSpot(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("owner_id")
    val ownerId: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("address")
    val address: String = "",

    @SerializedName("latitude")
    val latitude: Double = 0.0,

    @SerializedName("longitude")
    val longitude: Double = 0.0,

    @SerializedName("price_per_hour")
    val pricePerHour: Double = 0.0,

    @SerializedName("price_per_day")
    val pricePerDay: Double? = null,

    @SerializedName("price_unit")
    val priceUnit: String = "hour",

    @SerializedName("max_vehicle_height")
    val maxVehicleHeight: Double? = null,

    @SerializedName("max_vehicle_width")
    val maxVehicleWidth: Double? = null,

    @SerializedName("available_spots")
    val availableSpots: Int = 1,

    @SerializedName("total_spots")
    val totalSpots: Int = 1,

    @SerializedName("is_covered")
    val isCovered: Boolean = false,

    @SerializedName("has_lighting")
    val hasLighting: Boolean = false,

    @SerializedName("has_security")
    val hasSecurity: Boolean = false,

    @SerializedName("has_charging")
    val hasCharging: Boolean = false,

    @SerializedName("has_cctv")
    val hasCctv: Boolean = false,

    @SerializedName("is_24h_access")
    val is24hAccess: Boolean = false,

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("is_approved")
    val isApproved: Boolean = false,

    @SerializedName("view_count")
    val viewCount: Int = 0,

    @SerializedName("book_count")
    val bookCount: Int = 0,

    @SerializedName("owner_username")
    val ownerUsername: String? = null,

    @SerializedName("owner_avatar")
    val ownerAvatar: String? = null,

    @SerializedName("avg_rating")
    val avgRating: Double? = null,

    @SerializedName("review_count")
    val reviewCount: Int = 0,

    @SerializedName("primary_image")
    val primaryImage: String? = null,

    @SerializedName("is_favorite")
    val isFavorite: Boolean = false,

    @SerializedName("images")
    val images: List<SpotImage> = emptyList(),

    @SerializedName("availability")
    val availability: List<AvailabilitySlot> = emptyList(),

    @SerializedName("special_dates")
    val specialDates: List<SpecialDate> = emptyList(),

    @SerializedName("created_at")
    val createdAt: Date? = null,

    @SerializedName("updated_at")
    val updatedAt: Date? = null
)

/**
 * 停车位图片
 */
data class SpotImage(
    @SerializedName("image_url")
    val imageUrl: String,

    @SerializedName("image_order")
    val imageOrder: Int = 0,

    @SerializedName("is_primary")
    val isPrimary: Boolean = false
)

/**
 * 可用时间段
 */
data class AvailabilitySlot(
    @SerializedName("day_of_week")
    val dayOfWeek: Int, // 0=周日, 1=周一,..., 6=周六

    @SerializedName("start_time")
    val startTime: String, // HH:mm:ss格式

    @SerializedName("end_time")
    val endTime: String, // HH:mm:ss格式

    @SerializedName("is_available")
    val isAvailable: Boolean = true
)

/**
 * 特殊日期
 */
data class SpecialDate(
    @SerializedName("date")
    val date: String, // YYYY-MM-DD格式

    @SerializedName("start_time")
    val startTime: String? = "00:00:00",

    @SerializedName("end_time")
    val endTime: String? = "23:59:59",

    @SerializedName("status")
    val status: String, // available, unavailable, maintenance

    @SerializedName("notes")
    val notes: String? = null
)

/**
 * 创建停车位请求
 */
data class CreateParkingSpotRequest(
    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("address")
    val address: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("price_per_hour")
    val pricePerHour: Double,

    @SerializedName("price_per_day")
    val pricePerDay: Double? = null,

    @SerializedName("price_unit")
    val priceUnit: String = "hour",

    @SerializedName("max_vehicle_height")
    val maxVehicleHeight: Double? = null,

    @SerializedName("max_vehicle_width")
    val maxVehicleWidth: Double? = null,

    @SerializedName("available_spots")
    val availableSpots: Int = 1,

    @SerializedName("total_spots")
    val totalSpots: Int = 1,

    @SerializedName("is_covered")
    val isCovered: Boolean = false,

    @SerializedName("has_lighting")
    val hasLighting: Boolean = false,

    @SerializedName("has_security")
    val hasSecurity: Boolean = false,

    @SerializedName("has_charging")
    val hasCharging: Boolean = false,

    @SerializedName("has_cctv")
    val hasCctv: Boolean = false,

    @SerializedName("is_24h_access")
    val is24hAccess: Boolean = false,

    @SerializedName("availability")
    val availability: List<AvailabilitySlot>? = null,

    @SerializedName("images")
    val images: List<String>? = null
)

/**
 * 搜索过滤器
 */
data class ParkingSearchFilters(
    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("radius")
    val radius: Double? = null, // 公里

    @SerializedName("min_price")
    val minPrice: Double? = null,

    @SerializedName("max_price")
    val maxPrice: Double? = null,

    @SerializedName("keyword")
    val keyword: String? = null,

    @SerializedName("start_time")
    val startTime: String? = null, // ISO格式

    @SerializedName("end_time")
    val endTime: String? = null, // ISO格式

    @SerializedName("is_covered")
    val isCovered: Boolean? = null,

    @SerializedName("has_lighting")
    val hasLighting: Boolean? = null,

    @SerializedName("has_security")
    val hasSecurity: Boolean? = null,

    @SerializedName("has_charging")
    val hasCharging: Boolean? = null,

    @SerializedName("has_cctv")
    val hasCctv: Boolean? = null,

    @SerializedName("is_24h_access")
    val is24hAccess: Boolean? = null,

    @SerializedName("max_height")
    val maxHeight: Double? = null, // 米

    @SerializedName("max_width")
    val maxWidth: Double? = null, // 米

    @SerializedName("sort_by")
    val sortBy: String? = null, // price_asc, price_desc, rating, distance

    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("limit")
    val limit: Int = 20
)

/**
 * 搜索响应
 */
data class ParkingSearchResponse(
    @SerializedName("spots")
    val spots: List<ParkingSpot>,

    @SerializedName("pagination")
    val pagination: Pagination
)

data class Pagination(
    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("total")
    val total: Int,

    @SerializedName("pages")
    val pages: Int
)