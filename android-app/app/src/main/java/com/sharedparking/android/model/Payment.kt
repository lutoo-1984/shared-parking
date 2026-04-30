package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

/**
 * 支付数据模型
 */
data class Payment(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("booking_id")
    val bookingId: Int = 0,

    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("payment_method")
    val paymentMethod: String = "",

    @SerializedName("transaction_id")
    val transactionId: String? = null,

    @SerializedName("amount")
    val amount: Double = 0.0,

    @SerializedName("currency")
    val currency: String = "CNY",

    @SerializedName("status")
    val status: String = "pending",

    @SerializedName("refund_amount")
    val refundAmount: Double = 0.0,

    @SerializedName("refund_reason")
    val refundReason: String? = null,

    @SerializedName("refunded_at")
    val refundedAt: String? = null,

    @SerializedName("paid_at")
    val paidAt: String? = null,

    @SerializedName("payment_details")
    val paymentDetails: Map<String, Any>? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    // 关联信息
    @SerializedName("booking_info")
    val bookingInfo: Booking? = null,

    @SerializedName("spot_title")
    val spotTitle: String? = null,

    @SerializedName("spot_address")
    val spotAddress: String? = null
) {
    fun getPaymentMethodEnum(): PaymentMethod {
        return PaymentMethod.fromValue(paymentMethod)
    }

    fun getPaymentStatus(): PaymentStatus {
        return PaymentStatus.fromValue(status)
    }

    fun getPaidAtDate(): Date? {
        return parseDate(paidAt)
    }

    fun getRefundedAtDate(): Date? {
        return parseDate(refundedAt)
    }

    fun getCreatedAtDate(): Date? {
        return parseDate(createdAt)
    }

    fun getUpdatedAtDate(): Date? {
        return parseDate(updatedAt)
    }

    fun isPaid(): Boolean {
        return getPaymentStatus() == PaymentStatus.PAID
    }

    fun isPending(): Boolean {
        return getPaymentStatus() == PaymentStatus.PENDING
    }

    fun isFailed(): Boolean {
        return getPaymentStatus() == PaymentStatus.FAILED
    }

    fun isRefunded(): Boolean {
        return getPaymentStatus() == PaymentStatus.REFUNDED
    }

    fun canRefund(): Boolean {
        return isPaid() && refundAmount < amount
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

        fun formatCurrency(amount: Double, currency: String = "CNY"): String {
            return when (currency) {
                "CNY" -> "¥${String.format("%.2f", amount)}"
                "USD" -> "$${String.format("%.2f", amount)}"
                else -> "${String.format("%.2f", amount)} $currency"
            }
        }
    }
}