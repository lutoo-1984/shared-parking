package com.sharedparking.android.ui.payment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.sharedparking.android.databinding.ActivityPaymentDetailBinding
import com.sharedparking.android.model.Payment
import com.sharedparking.android.utils.Resource
import com.sharedparking.android.viewmodel.PaymentViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 支付详情页面
 */
class PaymentDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentDetailBinding
    private val viewModel: PaymentViewModel by viewModels()

    private var paymentId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取支付ID
        paymentId = intent.getIntExtra(EXTRA_PAYMENT_ID, 0)
        if (paymentId <= 0) {
            Toast.makeText(this, "支付信息无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 设置Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "支付详情"

        setupObservers()
        setupClickListeners()

        // 加载支付详情
        viewModel.getPaymentDetail(paymentId)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupObservers() {
        // 观察支付详情状态
        viewModel.paymentDetailState.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                    showErrorView(false)
                }
                is Resource.Success -> {
                    showLoading(false)
                    val payment = resource.data
                    if (payment != null) {
                        updateUI(payment)
                    } else {
                        showErrorView(true, "支付信息为空")
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    showErrorView(true, resource.message ?: "加载失败")
                }
                else -> {
                    // Idle状态
                }
            }
        })

        // 观察退款状态
        viewModel.refundState.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnRefund.isEnabled = false
                    binding.btnRefund.text = "退款处理中..."
                }
                is Resource.Success -> {
                    binding.btnRefund.isEnabled = false
                    binding.btnRefund.text = "已退款"
                    binding.btnRefund.setBackgroundColor(ContextCompat.getColor(this, com.sharedparking.android.R.color.text_disabled))

                    val payment = resource.data
                    if (payment != null) {
                        updateUI(payment)
                    }
                    Toast.makeText(this, "退款申请已提交", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.btnRefund.isEnabled = true
                    binding.btnRefund.text = "申请退款"
                    Toast.makeText(this, resource.message ?: "退款失败", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.btnRefund.isEnabled = true
                    binding.btnRefund.text = "申请退款"
                }
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnRefund.setOnClickListener {
            showRefundDialog()
        }

        binding.layoutError.btnRetry.setOnClickListener {
            viewModel.getPaymentDetail(paymentId)
        }
    }

    private fun updateUI(payment: Payment) {
        // 设置支付状态
        val status = payment.getPaymentStatus()
        binding.tvPaymentStatus.text = getStatusText(status)
        binding.tvPaymentStatus.background = getStatusBackground(status)

        // 设置支付金额
        binding.tvPaymentAmount.text = Payment.formatCurrency(payment.amount, payment.currency)

        // 设置支付方式
        val paymentMethod = payment.getPaymentMethodEnum()
        binding.tvPaymentMethod.text = paymentMethod.displayName
        binding.ivPaymentMethod.setImageResource(getPaymentMethodIcon(paymentMethod))
        binding.ivPaymentMethod.setColorFilter(getPaymentMethodColor(paymentMethod))

        // 设置交易号
        binding.tvTransactionId.text = payment.transactionId ?: "等待确认"

        // 设置支付时间
        val paidAt = payment.getPaidAtDate()
        binding.tvPaidTime.text = if (paidAt != null) {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            formatter.format(paidAt)
        } else {
            "等待支付"
        }

        // 设置创建时间
        val createdAt = payment.getCreatedAtDate()
        binding.tvCreatedTime.text = if (createdAt != null) {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            formatter.format(createdAt)
        } else {
            ""
        }

        // 设置退款信息（如果有）
        if (payment.isRefunded()) {
            binding.layoutRefundInfo.root.visibility = View.VISIBLE
            binding.layoutRefundInfo.tvRefundAmount.text = Payment.formatCurrency(payment.refundAmount, payment.currency)
            binding.layoutRefundInfo.tvRefundReason.text = payment.refundReason ?: "用户申请退款"

            val refundedAt = payment.getRefundedAtDate()
            binding.layoutRefundInfo.tvRefundTime.text = if (refundedAt != null) {
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                formatter.format(refundedAt)
            } else {
                ""
            }
        } else {
            binding.layoutRefundInfo.root.visibility = View.GONE
        }

        // 设置车位信息
        binding.tvSpotTitle.text = payment.spotTitle ?: "未知车位"
        binding.tvSpotAddress.text = payment.spotAddress ?: ""

        // 设置退款按钮状态
        updateRefundButton(payment)
    }

    private fun updateRefundButton(payment: Payment) {
        if (payment.canRefund()) {
            binding.btnRefund.visibility = View.VISIBLE
            binding.btnRefund.isEnabled = true
            binding.btnRefund.text = "申请退款"
        } else if (payment.isRefunded()) {
            binding.btnRefund.visibility = View.VISIBLE
            binding.btnRefund.isEnabled = false
            binding.btnRefund.text = "已退款"
            binding.btnRefund.setBackgroundColor(ContextCompat.getColor(this, com.sharedparking.android.R.color.text_disabled))
        } else {
            binding.btnRefund.visibility = View.GONE
        }
    }

    private fun showRefundDialog() {
        // 这里可以显示退款对话框
        // 暂时简单处理：直接申请全额退款
        viewModel.requestRefund(paymentId, "用户申请退款", null)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.scrollView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showErrorView(show: Boolean, message: String? = null) {
        binding.layoutError.root.visibility = if (show) View.VISIBLE else View.GONE
        binding.scrollView.visibility = if (show) View.GONE else View.VISIBLE
        if (show && message != null) {
            binding.layoutError.tvErrorMessage.text = message
        }
    }

    private fun getStatusText(status: com.sharedparking.android.model.PaymentStatus): String {
        return when (status) {
            com.sharedparking.android.model.PaymentStatus.PENDING -> "待支付"
            com.sharedparking.android.model.PaymentStatus.PAID -> "已支付"
            com.sharedparking.android.model.PaymentStatus.REFUNDED -> "已退款"
            com.sharedparking.android.model.PaymentStatus.FAILED -> "支付失败"
        }
    }

    private fun getStatusBackground(status: com.sharedparking.android.model.PaymentStatus): android.graphics.drawable.Drawable {
        val drawableId = when (status) {
            com.sharedparking.android.model.PaymentStatus.PENDING -> com.sharedparking.android.R.drawable.bg_status_pending
            com.sharedparking.android.model.PaymentStatus.PAID -> com.sharedparking.android.R.drawable.bg_status_paid
            com.sharedparking.android.model.PaymentStatus.REFUNDED -> com.sharedparking.android.R.drawable.bg_status_refunded
            com.sharedparking.android.model.PaymentStatus.FAILED -> com.sharedparking.android.R.drawable.bg_status_failed
        }
        return ContextCompat.getDrawable(this, drawableId)!!
    }

    private fun getPaymentMethodIcon(method: com.sharedparking.android.model.PaymentMethod): Int {
        return when (method) {
            com.sharedparking.android.model.PaymentMethod.ALIPAY -> com.sharedparking.android.R.drawable.ic_alipay
            com.sharedparking.android.model.PaymentMethod.WECHAT -> com.sharedparking.android.R.drawable.ic_wechat
            com.sharedparking.android.model.PaymentMethod.CREDIT_CARD -> com.sharedparking.android.R.drawable.ic_credit_card
            com.sharedparking.android.model.PaymentMethod.WALLET -> com.sharedparking.android.R.drawable.ic_wallet
        }
    }

    private fun getPaymentMethodColor(method: com.sharedparking.android.model.PaymentMethod): Int {
        val colorId = when (method) {
            com.sharedparking.android.model.PaymentMethod.ALIPAY -> com.sharedparking.android.R.color.payment_alipay
            com.sharedparking.android.model.PaymentMethod.WECHAT -> com.sharedparking.android.R.color.payment_wechat
            com.sharedparking.android.model.PaymentMethod.CREDIT_CARD -> com.sharedparking.android.R.color.payment_credit_card
            com.sharedparking.android.model.PaymentMethod.WALLET -> com.sharedparking.android.R.color.payment_wallet
        }
        return ContextCompat.getColor(this, colorId)
    }

    companion object {
        const val EXTRA_PAYMENT_ID = "extra_payment_id"
    }
}