package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName

/**
 * 退款请求
 */
data class RefundRequest(
    @SerializedName("reason")
    val reason: String,

    @SerializedName("amount")
    val amount: Double? = null,

    @SerializedName("refund_to_original")
    val refundToOriginal: Boolean = true
)