package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName

/**
 * 支付状态枚举
 */
enum class PaymentStatus(val value: String) {
    @SerializedName("pending")
    PENDING("pending"),

    @SerializedName("paid")
    PAID("paid"),

    @SerializedName("refunded")
    REFUNDED("refunded"),

    @SerializedName("failed")
    FAILED("failed");

    companion object {
        fun fromValue(value: String): PaymentStatus {
            return when (value) {
                "pending" -> PENDING
                "paid" -> PAID
                "refunded" -> REFUNDED
                "failed" -> FAILED
                else -> PENDING
            }
        }
    }
}