package com.sharedparking.android.ui.payment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sharedparking.android.R
import com.sharedparking.android.model.Payment
import com.sharedparking.android.ui.booking.BookingDetailActivity
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

    private var paymentId: Int = 0
    private var bookingId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        // 获取传递的数据 - 现在只传递ID
        paymentId = intent.getIntExtra("extra_payment_id", 0)
        bookingId = intent.getIntExtra("extra_booking_id", 0)

        initViews()
        setupUI()
        setupClickListeners()
    }

    private fun initViews() {
        tvPaymentAmount = findViewById(R.id.tvPaymentAmount)
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod)
        tvTransactionId = findViewById(R.id.tvTransactionId)
        tvPaidTime = findViewById(R.id.tvPaidTime)
        btnViewBooking = findViewById(R.id.btnViewBooking)
        btnBackToHome = findViewById(R.id.btnBackToHome)

        // 设置Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "支付成功"
    }

    private fun setupUI() {
        // TODO: 通过paymentId从服务器获取支付详情
        // 暂时显示占位信息
        tvPaymentAmount.text = "支付成功"
        tvPaymentMethod.text = "支付宝"
        tvTransactionId.text = "交易处理中"
        tvPaidTime.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // 根据支付方式设置图标颜色
        setPaymentMethodColor(com.sharedparking.android.model.PaymentMethod.ALIPAY)

        // 隐藏查看订单按钮如果没有bookingId
        if (bookingId <= 0) {
            btnViewBooking.visibility = View.GONE
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
            // 返回主页
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        // 返回时直接关闭，不返回支付页面
        setResult(RESULT_OK)
        super.onBackPressed()
    }

    companion object {
        const val EXTRA_PAYMENT = "extra_payment"
        const val EXTRA_BOOKING_ID = "extra_booking_id"
    }
}