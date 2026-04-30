package com.sharedparking.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sharedparking.android.model.*
import com.sharedparking.android.repository.PaymentRepository
import com.sharedparking.android.utils.Resource
import kotlinx.coroutines.launch

/**
 * 支付相关的ViewModel
 */
class PaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val paymentRepository = PaymentRepository(application)

    // 支付状态
    private val _createPaymentState = MutableLiveData<Resource<Payment>>()
    val createPaymentState: LiveData<Resource<Payment>> = _createPaymentState

    private val _paymentDetailState = MutableLiveData<Resource<Payment>>()
    val paymentDetailState: LiveData<Resource<Payment>> = _paymentDetailState

    private val _refundState = MutableLiveData<Resource<Payment>>()
    val refundState: LiveData<Resource<Payment>> = _refundState

    // 支付信息
    private val _bookingInfo = MutableLiveData<Booking?>()
    val bookingInfo: LiveData<Booking?> = _bookingInfo

    private val _selectedPaymentMethod = MutableLiveData<PaymentMethod>(PaymentMethod.ALIPAY)
    val selectedPaymentMethod: LiveData<PaymentMethod> = _selectedPaymentMethod

    private val _paymentAmount = MutableLiveData<Double>(0.0)
    val paymentAmount: LiveData<Double> = _paymentAmount

    // 可用的支付方式
    private val _availablePaymentMethods = MutableLiveData<List<PaymentMethod>>()
    val availablePaymentMethods: LiveData<List<PaymentMethod>> = _availablePaymentMethods

    // ===== 公开方法 =====

    /**
     * 设置预订信息
     */
    fun setBookingInfo(booking: Booking) {
        _bookingInfo.value = booking
        _paymentAmount.value = booking.totalPrice
        loadAvailablePaymentMethods()
    }

    /**
     * 设置支付方式
     */
    fun setPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }

    /**
     * 设置支付金额
     */
    fun setPaymentAmount(amount: Double) {
        _paymentAmount.value = amount
    }

    /**
     * 创建支付
     */
    fun createPayment() {
        val booking = _bookingInfo.value
        val paymentMethod = _selectedPaymentMethod.value
        val amount = _paymentAmount.value

        if (booking == null || paymentMethod == null || amount == null) {
            _createPaymentState.value = Resource.Error("请选择支付方式和金额")
            return
        }

        // 验证支付金额
        val (isValid, errorMessage) = paymentRepository.validatePaymentAmount(amount, booking.totalPrice)
        if (!isValid) {
            _createPaymentState.value = Resource.Error(errorMessage ?: "支付金额无效")
            return
        }

        viewModelScope.launch {
            _createPaymentState.value = Resource.Loading()
            try {
                val result = paymentRepository.createPayment(
                    bookingId = booking.id,
                    paymentMethod = paymentMethod,
                    amount = amount
                )

                _createPaymentState.value = result
            } catch (e: Exception) {
                _createPaymentState.value = Resource.Error(e.message ?: "创建支付失败")
            }
        }
    }

    /**
     * 获取支付详情
     */
    fun getPaymentDetail(paymentId: Int) {
        viewModelScope.launch {
            _paymentDetailState.value = Resource.Loading()
            try {
                val result = paymentRepository.getPayment(paymentId)
                _paymentDetailState.value = result
            } catch (e: Exception) {
                _paymentDetailState.value = Resource.Error(e.message ?: "获取支付详情失败")
            }
        }
    }

    /**
     * 获取预订的支付记录
     */
    fun getBookingPayment(bookingId: Int) {
        viewModelScope.launch {
            _paymentDetailState.value = Resource.Loading()
            try {
                val result = paymentRepository.getBookingPayment(bookingId)
                _paymentDetailState.value = result
            } catch (e: Exception) {
                _paymentDetailState.value = Resource.Error(e.message ?: "获取预订支付记录失败")
            }
        }
    }

    /**
     * 申请退款
     */
    fun requestRefund(paymentId: Int, reason: String, amount: Double? = null) {
        viewModelScope.launch {
            _refundState.value = Resource.Loading()
            try {
                val result = paymentRepository.requestRefund(
                    paymentId = paymentId,
                    reason = reason,
                    amount = amount
                )

                _refundState.value = result
            } catch (e: Exception) {
                _refundState.value = Resource.Error(e.message ?: "申请退款失败")
            }
        }
    }

    /**
     * 重置创建支付状态
     */
    fun resetCreatePaymentState() {
        _createPaymentState.value = Resource.Idle()
    }

    /**
     * 重置支付详情状态
     */
    fun resetPaymentDetailState() {
        _paymentDetailState.value = Resource.Idle()
    }

    /**
     * 重置退款状态
     */
    fun resetRefundState() {
        _refundState.value = Resource.Idle()
    }

    /**
     * 获取支付方式显示名称
     */
    fun getPaymentMethodDisplayName(method: PaymentMethod): String {
        return paymentRepository.getPaymentMethodDisplayName(method)
    }

    /**
     * 格式化金额显示
     */
    fun formatAmountDisplay(amount: Double): String {
        return paymentRepository.formatAmountDisplay(amount)
    }

    /**
     * 获取当前支付信息
     */
    fun getPaymentInfo(): PaymentInfo {
        return PaymentInfo(
            booking = _bookingInfo.value,
            paymentMethod = _selectedPaymentMethod.value ?: PaymentMethod.ALIPAY,
            amount = _paymentAmount.value ?: 0.0,
            currency = "CNY"
        )
    }

    /**
     * 加载可用的支付方式
     */
    private fun loadAvailablePaymentMethods() {
        val methods = paymentRepository.getAvailablePaymentMethods()
        _availablePaymentMethods.value = methods
    }

    /**
     * 支付信息数据类
     */
    data class PaymentInfo(
        val booking: Booking?,
        val paymentMethod: PaymentMethod,
        val amount: Double,
        val currency: String
    ) {
        val formattedAmount: String
            get() = Payment.formatCurrency(amount, currency)

        val paymentMethodDisplayName: String
            get() = paymentMethod.displayName
    }
}