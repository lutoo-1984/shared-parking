package com.sharedparking.android.ui.payment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sharedparking.android.R
import com.sharedparking.android.ui.booking.BookingDetailActivity
import com.sharedparking.android.viewmodel.PaymentViewModel
import com.sharedparking.android.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

/**
 * 支付成功页面
 */
class PaymentSuccessActivity : AppCompatActivity() {

    private lateinit var tvPaymentAmount: TextView
    private lateinit var tvPaymentMethod: TextView
    private lateinit var tvTransactionId: TextView
    private lateinit var tvPaidTime: TextView
    private lateinit var btnViewBooking: Button
    private lateinit var btnBackToHome: Button
    private lateinit var viewModel: PaymentViewModel

    private var paymentId: Int = 0
    private var bookingId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        paymentId = intent.getIntExtra("extra_payment_id", 0)
        bookingId = intent.getIntExtra("extra_booking_id", 0)

        viewModel = ViewModelProvider(this)[PaymentViewModel::class.java]

        initViews()
        setupUI()
        setupClickListeners()
        observeData()

        // 如果有paymentId，从服务器获取支付详情
        if (paymentId > 0) {
            viewModel.getPaymentDetail(paymentId)
        }
    }

    private fun initViews() {
        tvPaymentAmount = findViewById(R.id.tvPaymentAmount)
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod)
        tvTransactionId = findViewById(R.id.tvTransactionId)
        tvPaidTime = findViewById(R.id.tvPaidTime)
        btnViewBooking = findViewById(R.id.btnViewBooking)
        btnBackToHome = findViewById(R.id.btnBackToHome)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "支付成功"
    }

    private fun setupUI() {
        // 显示占位信息，API返回后更新
        tvPaymentAmount.text = "支付成功"
        tvPaymentMethod.text = "处理中..."
        tvTransactionId.text = "交易处理中"
        tvPaidTime.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        if (bookingId <= 0) {
            btnViewBooking.visibility = View.GONE
        }
    }

    private fun observeData() {
        viewModel.paymentDetailState.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val payment = resource.data
                    if (payment != null) {
                        tvPaymentAmount.text = String.format("¥%.2f", payment.amount)
                        tvTransactionId.text = payment.transactionId ?: "N/A"
                        tvPaidTime.text = payment.paidAt ?: SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                        val methodText = when (payment.paymentMethod) {
                            "alipay" -> "支付宝"
                            "wechat" -> "微信支付"
                            "credit_card" -> "信用卡"
                            "wallet" -> "钱包支付"
                            else -> payment.paymentMethod ?: "未知"
                        }
                        tvPaymentMethod.text = methodText

                        val method = com.sharedparking.android.model.PaymentMethod.fromString(payment.paymentMethod)
                        val colorRes = when (method) {
                            com.sharedparking.android.model.PaymentMethod.ALIPAY -> R.color.payment_alipay
                            com.sharedparking.android.model.PaymentMethod.WECHAT -> R.color.payment_wechat
                            com.sharedparking.android.model.PaymentMethod.CREDIT_CARD -> R.color.payment_credit_card
                            com.sharedparking.android.model.PaymentMethod.WALLET -> R.color.payment_wallet
                        }
                        tvPaymentMethod.setTextColor(getColor(colorRes))

                        if (payment.bookingId > 0) {
                            bookingId = payment.bookingId
                            btnViewBooking.visibility = View.VISIBLE
                        }
                    }
                }
                is Resource.Error -> {
                    // API调用失败，显示默认数据
                    tvPaymentAmount.text = "支付成功"
                    tvPaymentMethod.text = "支付宝"
                    tvTransactionId.text = "交易处理中"
                }
                else -> {}
            }
        }
    }

    private fun setPaymentMethodColor(method: com.sharedparking.android.model.PaymentMethod) {
        val colorRes = when (method) {
            com.sharedparking.android.model.PaymentMethod.ALIPAY -> R.color.payment_alipay
            com.sharedparking.android.model.PaymentMethod.WECHAT -> R.color.payment_wechat
            com.sharedparking.android.model.PaymentMethod.CREDIT_CARD -> R.color.payment_credit_card
            com.sharedparking.android.model.PaymentMethod.WALLET -> R.color.payment_wallet
        }
        tvPaymentMethod.setTextColor(getColor(colorRes))
    }

    private fun setupClickListeners() {
        btnViewBooking.setOnClickListener {
            if (bookingId > 0) {
                val intent = Intent(this, BookingDetailActivity::class.java).apply {
                    putExtra("extra_booking_id", bookingId)
                }
                startActivity(intent)
            }
            finish()
        }

        btnBackToHome.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        super.onBackPressed()
    }

    companion object {
        const val EXTRA_PAYMENT = "extra_payment"
        const val EXTRA_BOOKING_ID = "extra_booking_id"
    }
}
