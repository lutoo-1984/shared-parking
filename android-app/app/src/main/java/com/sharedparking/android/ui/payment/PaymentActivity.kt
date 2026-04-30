package com.sharedparking.android.ui.payment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.sharedparking.android.databinding.ActivityPaymentBinding
import com.sharedparking.android.model.Booking
import com.sharedparking.android.model.PaymentMethod
import com.sharedparking.android.utils.Resource
import com.sharedparking.android.viewmodel.PaymentViewModel

/**
 * 支付Activity
 * 处理订单支付功能
 */
class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private val viewModel: PaymentViewModel by viewModels()

    private var currentBooking: Booking? = null
    private var selectedPaymentMethod: PaymentMethod = PaymentMethod.ALIPAY
    private var bookingId: Int = 0

    private var spotTitle: String? = null
    private var totalPrice: Double = 0.0
    private var startTime: String? = null
    private var endTime: String? = null
    private var durationHours: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // 获取传递的数据
        bookingId = intent.getIntExtra(EXTRA_BOOKING_ID, -1)
        spotTitle = intent.getStringExtra(EXTRA_SPOT_TITLE)
        totalPrice = intent.getDoubleExtra(EXTRA_TOTAL_PRICE, 0.0)
        startTime = intent.getStringExtra(EXTRA_START_TIME)
        endTime = intent.getStringExtra(EXTRA_END_TIME)
        durationHours = intent.getDoubleExtra(EXTRA_DURATION_HOURS, 0.0)

        if (bookingId == -1) {
            Toast.makeText(this, "订单信息无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 从Intent数据创建轻量Booking对象用于UI展示
        currentBooking = Booking(
            id = bookingId,
            spotTitle = spotTitle,
            totalPrice = totalPrice,
            startTime = startTime,
            endTime = endTime,
            durationHours = durationHours
        )

        // 设置到ViewModel
        currentBooking?.let { viewModel.setBookingInfo(it) }

        // 初始化UI
        initUI()
        setupObservers()
        setupClickListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initUI() {
        currentBooking?.let { booking ->
            // 设置订单信息
            binding.tvSpotTitle.text = booking.spotTitle ?: "未知车位"
            binding.tvBookingTime.text = formatBookingTime(booking)
            binding.tvOrderAmount.text = formatAmount(booking.totalPrice)
            binding.tvPaymentAmount.text = formatAmount(booking.totalPrice)
            binding.tvBottomPaymentAmount.text = formatAmount(booking.totalPrice)

            // 默认选择支付宝
            updatePaymentMethodSelection(PaymentMethod.ALIPAY)
        }
    }

    private fun setupObservers() {
        // 观察支付创建状态
        viewModel.createPaymentState.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    val payment = resource.data
                    if (payment != null) {
                        showPaymentSuccess(payment)
                    } else {
                        Toast.makeText(this@PaymentActivity, "支付创建失败", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError(resource.message ?: "支付失败")
                }
                else -> {
                    // Idle状态，不做处理
                }
            }
        })

        // 观察可用的支付方式
        viewModel.availablePaymentMethods.observe(this, Observer { methods ->
            // 可以在这里更新UI，比如禁用某些支付方式
        })

        // 观察选中的支付方式
        viewModel.selectedPaymentMethod.observe(this, Observer { method ->
            selectedPaymentMethod = method
            updatePaymentMethodSelection(method)
        })
    }

    private fun setupClickListeners() {
        // 支付方式选择
        binding.layoutAlipay.setOnClickListener {
            viewModel.setPaymentMethod(PaymentMethod.ALIPAY)
        }

        binding.layoutWechat.setOnClickListener {
            viewModel.setPaymentMethod(PaymentMethod.WECHAT)
        }

        binding.layoutCreditCard.setOnClickListener {
            viewModel.setPaymentMethod(PaymentMethod.CREDIT_CARD)
        }

        binding.layoutWallet.setOnClickListener {
            viewModel.setPaymentMethod(PaymentMethod.WALLET)
        }

        // 确认支付按钮
        binding.btnConfirmPayment.setOnClickListener {
            if (validatePayment()) {
                viewModel.createPayment()
            }
        }
    }

    private fun updatePaymentMethodSelection(method: PaymentMethod) {
        // 重置所有选择状态
        val defaultTint = ContextCompat.getColor(this, com.sharedparking.android.R.color.text_disabled)
        val selectedTint = ContextCompat.getColor(this, com.sharedparking.android.R.color.brand_primary)

        binding.ivAlipaySelected.setColorFilter(defaultTint)
        binding.ivWechatSelected.setColorFilter(defaultTint)
        binding.ivCreditCardSelected.setColorFilter(defaultTint)
        binding.ivWalletSelected.setColorFilter(defaultTint)

        // 设置选中的支付方式
        when (method) {
            PaymentMethod.ALIPAY -> {
                binding.ivAlipaySelected.setColorFilter(selectedTint)
            }
            PaymentMethod.WECHAT -> {
                binding.ivWechatSelected.setColorFilter(selectedTint)
            }
            PaymentMethod.CREDIT_CARD -> {
                binding.ivCreditCardSelected.setColorFilter(selectedTint)
            }
            PaymentMethod.WALLET -> {
                binding.ivWalletSelected.setColorFilter(selectedTint)
            }
        }

        // 更新支付方式显示
        binding.tvPaymentNote.text = getPaymentNote(method)
    }

    private fun validatePayment(): Boolean {
        currentBooking?.let { booking ->
            if (booking.totalPrice <= 0) {
                showError("支付金额无效")
                return false
            }
        }

        return true
    }

    private fun showLoading(show: Boolean) {
        binding.btnConfirmPayment.isEnabled = !show
        binding.btnConfirmPayment.text = if (show) "支付中..." else "立即支付"
    }

    private fun showPaymentSuccess(payment: com.sharedparking.android.model.Payment) {
        // 跳转到支付成功页面 - 只传递ID
        val intent = Intent(this, PaymentSuccessActivity::class.java).apply {
            putExtra("extra_payment_id", payment.id)
            currentBooking?.let {
                putExtra(PaymentSuccessActivity.EXTRA_BOOKING_ID, it.id)
            }
        }
        startActivity(intent)

        // 结束当前支付页面
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun formatBookingTime(booking: Booking): String {
        return "${booking.startTime} - ${booking.endTime} (${booking.durationHours}小时)"
    }

    private fun formatAmount(amount: Double): String {
        return "¥${String.format("%.2f", amount)}"
    }

    private fun getPaymentNote(method: PaymentMethod): String {
        return when (method) {
            PaymentMethod.ALIPAY -> "使用支付宝支付，安全快捷"
            PaymentMethod.WECHAT -> "使用微信支付，方便快捷"
            PaymentMethod.CREDIT_CARD -> "使用信用卡支付，支持国际卡"
            PaymentMethod.WALLET -> "使用钱包余额支付，无需跳转"
        }
    }

    companion object {
        const val EXTRA_BOOKING_ID = "extra_booking_id"
        const val EXTRA_BOOKING = "extra_booking"
        const val EXTRA_SPOT_TITLE = "extra_spot_title"
        const val EXTRA_TOTAL_PRICE = "extra_total_price"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_END_TIME = "extra_end_time"
        const val EXTRA_DURATION_HOURS = "extra_duration_hours"
        const val RESULT_PAYMENT_SUCCESS = 1001
    }
}