package com.sharedparking.android.model

/**
 * 车辆信息数据类
 */
data class VehicleInfo(
    val plateNumber: String = "",
    val brand: String = "",
    val model: String = "",
    val color: String = ""
) {
    fun isValid(): Boolean {
        return plateNumber.isNotBlank() && brand.isNotBlank() && model.isNotBlank()
    }
}