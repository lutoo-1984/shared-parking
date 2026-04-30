package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName

/**
 * 可用性检查响应
 */
data class AvailabilityResponse(
    @SerializedName("available")
    val available: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("conflicting_bookings")
    val conflictingBookings: List<Booking> = emptyList()
)