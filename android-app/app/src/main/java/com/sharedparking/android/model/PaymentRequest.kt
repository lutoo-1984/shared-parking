package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName

/**
 * 创建支付请求
 */
data class PaymentRequest(
    @SerializedName("booking_id")
    val bookingId: Int,

    @SerializedName("payment_method")
    val paymentMethod: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("currency")
    val currency: String = "CNY"
)