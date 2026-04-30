package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 预订数据模型
 */
data class Booking(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("spot_id")
    val spotId: Int = 0,

    @SerializedName("vehicle_plate_number")
    val vehiclePlateNumber: String? = null,

    @SerializedName("vehicle_brand")
    val vehicleBrand: String? = null,

    @SerializedName("vehicle_model")
    val vehicleModel: String? = null,

    @SerializedName("vehicle_color")
    val vehicleColor: String? = null,

    @SerializedName("start_time")
    val startTime: String? = null,

    @SerializedName("end_time")
    val endTime: String? = null,

    @SerializedName("duration_hours")
    val durationHours: Double = 0.0,

    @SerializedName("total_price")
    val totalPrice: Double = 0.0,

    @SerializedName("status")
    val status: String = "pending",

    @SerializedName("cancelled_by")
    val cancelledBy: String? = null,

    @SerializedName("cancellation_reason")
    val cancellationReason: String? = null,

    @SerializedName("cancelled_at")
    val cancelledAt: String? = null,

    @SerializedName("check_in_code")
    val checkInCode: String? = null,

    @SerializedName("check_in_at")
    val checkInAt: String? = null,

    @SerializedName("check_out_at")
    val checkOutAt: String? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    // 关联信息
    @SerializedName("spot_title")
    val spotTitle: String? = null,

    @SerializedName("spot_address")
    val spotAddress: String? = null,

    @SerializedName("price_per_hour")
    val pricePerHour: Double = 0.0,

    @SerializedName("owner_username")
    val ownerUsername: String? = null,

    @SerializedName("spot_images")
    val spotImages: List<String> = emptyList()
) {
    fun getStartTimeDate(): Date? {
        return parseDate(startTime)
    }

    fun getEndTimeDate(): Date? {
        return parseDate(endTime)
    }

    fun getCreatedAtDate(): Date? {
        return parseDate(createdAt)
    }

    fun getUpdatedAtDate(): Date? {
        return parseDate(updatedAt)
    }

    fun getCancelledAtDate(): Date? {
        return parseDate(cancelledAt)
    }

    fun getCheckInAtDate(): Date? {
        return parseDate(checkInAt)
    }

    fun getCheckOutAtDate(): Date? {
        return parseDate(checkOutAt)
    }

    fun getBookingStatus(): BookingStatus {
        return BookingStatus.fromValue(status)
    }

    fun isActive(): Boolean {
        val status = getBookingStatus()
        return status == BookingStatus.PENDING ||
                status == BookingStatus.CONFIRMED ||
                status == BookingStatus.IN_PROGRESS
    }

    fun canCancel(): Boolean {
        val status = getBookingStatus()
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED
    }

    fun canCheckIn(): Boolean {
        val status = getBookingStatus()
        return status == BookingStatus.CONFIRMED
    }

    fun canCheckOut(): Boolean {
        val status = getBookingStatus()
        return status == BookingStatus.IN_PROGRESS
    }

    private fun parseDate(dateString: String?): Date? {
        if (dateString.isNullOrEmpty()) return null
        return try {
            val formats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            )
            for (format in formats) {
                try {
                    return format.parse(dateString)
                } catch (e: Exception) {
                    continue
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private val displayFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun formatDisplayDate(date: Date?): String {
            return if (date != null) displayFormatter.format(date) else ""
        }
    }
}