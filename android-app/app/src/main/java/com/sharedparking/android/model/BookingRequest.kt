package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 创建预订请求
 */
data class BookingRequest(
    @SerializedName("spot_id")
    val spotId: Int,

    @SerializedName("vehicle_plate_number")
    val vehiclePlateNumber: String,

    @SerializedName("vehicle_brand")
    val vehicleBrand: String,

    @SerializedName("vehicle_model")
    val vehicleModel: String,

    @SerializedName("vehicle_color")
    val vehicleColor: String? = null,

    @SerializedName("start_time")
    val startTime: String, // ISO格式

    @SerializedName("end_time")
    val endTime: String, // ISO格式

    @SerializedName("notes")
    val notes: String? = null
) {
    companion object {
        private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

        fun create(
            spotId: Int,
            vehiclePlateNumber: String,
            vehicleBrand: String,
            vehicleModel: String,
            vehicleColor: String?,
            startTime: Date,
            endTime: Date,
            notes: String?
        ): BookingRequest {
            return BookingRequest(
                spotId = spotId,
                vehiclePlateNumber = vehiclePlateNumber,
                vehicleBrand = vehicleBrand,
                vehicleModel = vehicleModel,
                vehicleColor = vehicleColor,
                startTime = isoFormatter.format(startTime),
                endTime = isoFormatter.format(endTime),
                notes = notes
            )
        }
    }
}