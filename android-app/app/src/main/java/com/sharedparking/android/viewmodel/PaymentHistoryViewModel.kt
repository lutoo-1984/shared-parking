package com.sharedparking.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sharedparking.android.model.Payment
import com.sharedparking.android.repository.PaymentRepository
import com.sharedparking.android.utils.Resource
import kotlinx.coroutines.launch

/**
 * 支付记录ViewModel
 */
class PaymentHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val paymentRepository = PaymentRepository(application)

    // 支付记录状态
    private val _paymentsState = MutableLiveData<Resource<List<Payment>>>()
    val paymentsState: LiveData<Resource<List<Payment>>> = _paymentsState

    // 刷新状态
    private val _refreshState = MutableLiveData<Boolean>(false)
    val refreshState: LiveData<Boolean> = _refreshState

    // 当前页码
    private var currentPage = 1
    private val pageSize = 20
    private var hasMore = true
    private var isLoading = false

    // ===== 公开方法 =====

    /**
     * 加载支付记录
     */
    fun loadPayments() {
        if (isLoading) return

        isLoading = true
        viewModelScope.launch {
            _paymentsState.value = Resource.Loading()

            try {
                val result = paymentRepository.getMyPayments(currentPage, pageSize)
                if (result is Resource.Success) {
                    val payments = result.data?.payments ?: emptyList()
                    hasMore = payments.size >= pageSize

                    if (currentPage == 1) {
                        _paymentsState.value = Resource.Success(payments)
                    } else {
                        // 加载更多
                        val currentPayments = (_paymentsState.value as? Resource.Success)?.data ?: emptyList()
                        _paymentsState.value = Resource.Success(currentPayments + payments)
                    }
                } else {
                    _paymentsState.value = Resource.Error(result.message ?: "加载失败")
                }
            } catch (e: Exception) {
                _paymentsState.value = Resource.Error(e.message ?: "网络错误")
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * 刷新支付记录
     */
    fun refreshPayments() {
        if (isLoading) return

        _refreshState.value = true
        currentPage = 1
        hasMore = true

        viewModelScope.launch {
            try {
                val result = paymentRepository.getMyPayments(currentPage, pageSize)
                if (result is Resource.Success) {
                    val payments = result.data?.payments ?: emptyList()
                    hasMore = payments.size >= pageSize
                    _paymentsState.value = Resource.Success(payments)
                } else {
                    _paymentsState.value = Resource.Error(result.message ?: "刷新失败")
                }
            } catch (e: Exception) {
                _paymentsState.value = Resource.Error(e.message ?: "网络错误")
            } finally {
                isLoading = false
                _refreshState.value = false
            }
        }
    }

    /**
     * 加载更多支付记录
     */
    fun loadMorePayments() {
        if (isLoading || !hasMore) return

        currentPage++
        loadPayments()
    }

    /**
     * 获取支付状态显示文本
     */
    fun getPaymentStatusText(payment: Payment): String {
        return when (payment.getPaymentStatus()) {
            com.sharedparking.android.model.PaymentStatus.PENDING -> "待支付"
            com.sharedparking.android.model.PaymentStatus.PAID -> "已支付"
            com.sharedparking.android.model.PaymentStatus.REFUNDED -> "已退款"
            com.sharedparking.android.model.PaymentStatus.FAILED -> "支付失败"
        }
    }

    /**
     * 获取支付状态颜色资源
     */
    fun getPaymentStatusColorRes(payment: Payment): Int {
        return when (payment.getPaymentStatus()) {
            com.sharedparking.android.model.PaymentStatus.PENDING -> com.sharedparking.android.R.color.text_warning
            com.sharedparking.android.model.PaymentStatus.PAID -> com.sharedparking.android.R.color.text_success
            com.sharedparking.android.model.PaymentStatus.REFUNDED -> com.sharedparking.android.R.color.text_info
            com.sharedparking.android.model.PaymentStatus.FAILED -> com.sharedparking.android.R.color.text_error
        }
    }

    /**
     * 获取支付方式显示文本
     */
    fun getPaymentMethodText(payment: Payment): String {
        return payment.getPaymentMethodEnum().displayName
    }

    /**
     * 格式化支付时间显示
     */
    fun formatPaymentTime(payment: Payment): String {
        val date = payment.getCreatedAtDate() ?: payment.getPaidAtDate()
        return if (date != null) {
            val formatter = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
            formatter.format(date)
        } else {
            ""
        }
    }

    /**
     * 检查是否还有更多数据
     */
    fun hasMoreData(): Boolean {
        return hasMore
    }

    /**
     * 检查是否正在加载
     */
    fun isLoading(): Boolean {
        return isLoading
    }

    /**
     * 重置状态
     */
    fun resetState() {
        currentPage = 1
        hasMore = true
        isLoading = false
        _paymentsState.value = Resource.Idle()
        _refreshState.value = false
    }
}