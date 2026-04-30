package com.sharedparking.android.model

/**
 * 预订表单状态
 */
data class BookingFormState(
    val spotIdError: Int? = null,
    val startTimeError: Int? = null,
    val endTimeError: Int? = null,
    val plateNumberError: Int? = null,
    val vehicleBrandError: Int? = null,
    val vehicleModelError: Int? = null,
    val isDataValid: Boolean = false
)