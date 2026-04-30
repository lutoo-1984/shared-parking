package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName

/**
 * 预订状态枚举
 */
enum class BookingStatus(val value: String) {
    @SerializedName("pending")
    PENDING("pending"),

    @SerializedName("confirmed")
    CONFIRMED("confirmed"),

    @SerializedName("in_progress")
    IN_PROGRESS("in_progress"),

    @SerializedName("completed")
    COMPLETED("completed"),

    @SerializedName("cancelled")
    CANCELLED("cancelled"),

    @SerializedName("expired")
    EXPIRED("expired");

    companion object {
        fun fromValue(value: String): BookingStatus {
            return when (value) {
                "pending" -> PENDING
                "confirmed" -> CONFIRMED
                "in_progress" -> IN_PROGRESS
                "completed" -> COMPLETED
                "cancelled" -> CANCELLED
                "expired" -> EXPIRED
                else -> PENDING
            }
        }
    }
}