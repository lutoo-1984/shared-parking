package com.sharedparking.android.ui.payment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharedparking.android.databinding.ItemPaymentHistoryBinding
import com.sharedparking.android.model.Payment
import com.sharedparking.android.model.PaymentMethod

/**
 * 支付历史适配器
 */
class PaymentHistoryAdapter(
    private val onItemClick: (Payment) -> Unit
) : ListAdapter<Payment, PaymentHistoryAdapter.PaymentViewHolder>(PaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PaymentViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = getItem(position)
        holder.bind(payment)
    }

    class PaymentViewHolder(
        private val binding: ItemPaymentHistoryBinding,
        private val onItemClick: (Payment) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: Payment) {
            binding.root.setOnClickListener {
                onItemClick(payment)
            }

            // 设置车位标题
            binding.tvSpotTitle.text = payment.spotTitle ?: "未知车位"

            // 设置支付金额
            binding.tvPaymentAmount.text = Payment.formatCurrency(payment.amount, payment.currency)

            // 设置支付时间
            val date = payment.getCreatedAtDate() ?: payment.getPaidAtDate()
            binding.tvPaymentTime.text = if (date != null) {
                val formatter = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
                formatter.format(date)
            } else {
                ""
            }

            // 设置支付状态
            val status = payment.getPaymentStatus()
            binding.tvPaymentStatus.text = getStatusText(status)
            binding.tvPaymentStatus.background = getStatusBackground(status, binding.root.context)

            // 设置支付方式
            val paymentMethod = payment.getPaymentMethodEnum()
            binding.tvPaymentMethod.text = paymentMethod.displayName
            binding.ivPaymentMethod.setImageResource(getPaymentMethodIcon(paymentMethod))
            binding.ivPaymentMethod.setColorFilter(getPaymentMethodColor(paymentMethod, binding.root.context))

            // 设置交易号（显示后6位）
            val transactionId = payment.transactionId
            binding.tvTransactionId.text = if (!transactionId.isNullOrEmpty() && transactionId.length > 6) {
                "${transactionId.takeLast(6)}..."
            } else {
                transactionId ?: ""
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

        private fun getStatusBackground(
            status: com.sharedparking.android.model.PaymentStatus,
            context: android.content.Context
        ): android.graphics.drawable.Drawable {
            val drawableId = when (status) {
                com.sharedparking.android.model.PaymentStatus.PENDING -> com.sharedparking.android.R.drawable.bg_status_pending
                com.sharedparking.android.model.PaymentStatus.PAID -> com.sharedparking.android.R.drawable.bg_status_paid
                com.sharedparking.android.model.PaymentStatus.REFUNDED -> com.sharedparking.android.R.drawable.bg_status_refunded
                com.sharedparking.android.model.PaymentStatus.FAILED -> com.sharedparking.android.R.drawable.bg_status_failed
            }
            return ContextCompat.getDrawable(context, drawableId)!!
        }

        private fun getPaymentMethodIcon(method: PaymentMethod): Int {
            return when (method) {
                PaymentMethod.ALIPAY -> com.sharedparking.android.R.drawable.ic_alipay
                PaymentMethod.WECHAT -> com.sharedparking.android.R.drawable.ic_wechat
                PaymentMethod.CREDIT_CARD -> com.sharedparking.android.R.drawable.ic_credit_card
                PaymentMethod.WALLET -> com.sharedparking.android.R.drawable.ic_wallet
            }
        }

        private fun getPaymentMethodColor(
            method: PaymentMethod,
            context: android.content.Context
        ): Int {
            val colorId = when (method) {
                PaymentMethod.ALIPAY -> com.sharedparking.android.R.color.payment_alipay
                PaymentMethod.WECHAT -> com.sharedparking.android.R.color.payment_wechat
                PaymentMethod.CREDIT_CARD -> com.sharedparking.android.R.color.payment_credit_card
                PaymentMethod.WALLET -> com.sharedparking.android.R.color.payment_wallet
            }
            return ContextCompat.getColor(context, colorId)
        }
    }

    private class PaymentDiffCallback : DiffUtil.ItemCallback<Payment>() {
        override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean {
            return oldItem == newItem
        }
    }
}