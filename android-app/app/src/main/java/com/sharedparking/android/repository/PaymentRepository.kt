package com.sharedparking.android.repository

import android.content.Context
import com.sharedparking.android.model.*
import com.sharedparking.android.network.ApiClientBuilder
import com.sharedparking.android.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 支付仓库类
 * 处理支付相关的API调用
 */
class PaymentRepository(private val context: Context) {

    private val apiService by lazy {
        ApiClientBuilder.getApiService()
    }

    // ===== 公开方法 =====

    /**
     * 创建支付
     */
    suspend fun createPayment(
        bookingId: Int,
        paymentMethod: PaymentMethod,
        amount: Double,
        currency: String = "CNY"
    ): Resource<Payment> {
        return withContext(Dispatchers.IO) {
            try {
                val request = PaymentRequest(
                    bookingId = bookingId,
                    paymentMethod = paymentMethod.value,
                    amount = amount,
                    currency = currency
                )

                val response = apiService.createPayment(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val payment = body.data as Payment
                        Resource.Success(payment)
                    } else {
                        Resource.Error(body?.message ?: "创建支付失败")
                    }
                } else {
                    Resource.Error("网络请求失败: ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "网络错误")
            }
        }
    }

    /**
     * 获取支付详情
     */
    suspend fun getPayment(paymentId: Int): Resource<Payment> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPayment(paymentId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val payment = body.data as Payment
                        Resource.Success(payment)
                    } else {
                        Resource.Error(body?.message ?: "获取支付详情失败")
                    }
                } else {
                    Resource.Error("网络请求失败: ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "网络错误")
            }
        }
    }

    /**
     * 获取预订的支付记录
     */
    suspend fun getBookingPayment(bookingId: Int): Resource<Payment> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBookingPayment(bookingId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val payment = body.data as Payment
                        Resource.Success(payment)
                    } else {
                        Resource.Error(body?.message ?: "获取预订支付记录失败")
                    }
                } else {
                    Resource.Error("网络请求失败: ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "网络错误")
            }
        }
    }

    /**
     * 获取我的支付记录
     */
    suspend fun getMyPayments(page: Int = 1, limit: Int = 20): Resource<PaymentListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyPayments(page, limit)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val paymentList = body.data as PaymentListResponse
                        Resource.Success(paymentList)
                    } else {
                        Resource.Error(body?.message ?: "获取支付记录失败")
                    }
                } else {
                    Resource.Error("网络请求失败: ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "网络错误")
            }
        }
    }

    /**
     * 申请退款
     */
    suspend fun requestRefund(
        paymentId: Int,
        reason: String,
        amount: Double? = null,
        refundToOriginal: Boolean = true
    ): Resource<Payment> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RefundRequest(
                    reason = reason,
                    amount = amount,
                    refundToOriginal = refundToOriginal
                )

                val response = apiService.requestRefund(paymentId, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val payment = body.data as Payment
                        Resource.Success(payment)
                    } else {
                        Resource.Error(body?.message ?: "申请退款失败")
                    }
                } else {
                    Resource.Error("网络请求失败: ${response.code()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "网络错误")
            }
        }
    }

    /**
     * 验证支付金额
     */
    fun validatePaymentAmount(amount: Double, bookingAmount: Double): Pair<Boolean, String?> {
        if (amount <= 0) {
            return Pair(false, "支付金额必须大于0")
        }

        if (amount > bookingAmount * 1.1) { // 允许10%的浮动
            return Pair(false, "支付金额超过预订金额过多")
        }

        return Pair(true, null)
    }

    /**
     * 获取支付方式显示名称
     */
    fun getPaymentMethodDisplayName(method: PaymentMethod): String {
        return method.displayName
    }

    /**
     * 获取所有可用的支付方式
     */
    fun getAvailablePaymentMethods(): List<PaymentMethod> {
        return listOf(
            PaymentMethod.ALIPAY,
            PaymentMethod.WECHAT,
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.WALLET
        )
    }

    /**
     * 格式化金额显示
     */
    fun formatAmountDisplay(amount: Double, currency: String = "CNY"): String {
        return Payment.formatCurrency(amount, currency)
    }
}