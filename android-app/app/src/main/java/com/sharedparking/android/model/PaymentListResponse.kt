package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName

/**
 * 支付列表响应
 */
data class PaymentListResponse(
    @SerializedName("payments")
    val payments: List<Payment> = emptyList(),

    @SerializedName("total")
    val total: Int = 0,

    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("limit")
    val limit: Int = 20,

    @SerializedName("total_pages")
    val totalPages: Int = 1
)