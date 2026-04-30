package com.sharedparking.android.ui.payment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharedparking.android.databinding.ActivityPaymentHistoryBinding
import com.sharedparking.android.model.Payment
import com.sharedparking.android.ui.payment.adapter.PaymentHistoryAdapter
import com.sharedparking.android.utils.Resource
import com.sharedparking.android.viewmodel.PaymentHistoryViewModel

/**
 * 支付记录页面
 * 显示用户的支付历史
 */
class PaymentHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentHistoryBinding
    private val viewModel: PaymentHistoryViewModel by viewModels()
    private lateinit var adapter: PaymentHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "支付记录"

        initRecyclerView()
        setupObservers()
        setupRefreshListener()

        // 加载支付记录
        viewModel.loadPayments()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initRecyclerView() {
        adapter = PaymentHistoryAdapter { payment ->
            // 点击支付记录，跳转到支付详情
            showPaymentDetail(payment)
        }

        binding.recyclerViewPayments.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPayments.adapter = adapter
    }

    private fun setupObservers() {
        // 观察支付记录加载状态
        viewModel.paymentsState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                    showEmptyView(false)
                    showErrorView(false)
                }
                is Resource.Success -> {
                    showLoading(false)
                    val payments = resource.data ?: emptyList()
                    adapter.submitList(payments)

                    if (payments.isEmpty()) {
                        showEmptyView(true)
                    } else {
                        showEmptyView(false)
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
        }

        // 观察刷新状态
        viewModel.refreshState.observe(this) { isRefreshing ->
            binding.swipeRefresh.isRefreshing = isRefreshing
        }
    }

    private fun setupRefreshListener() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshPayments()
        }
    }

    private fun showPaymentDetail(payment: Payment) {
        val intent = Intent(this, PaymentDetailActivity::class.java).apply {
            putExtra(PaymentDetailActivity.EXTRA_PAYMENT_ID, payment.id)
        }
        startActivity(intent)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyView(show: Boolean) {
        binding.layoutEmpty.root.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.layoutEmpty.tvEmptyTitle.text = "暂无支付记录"
            binding.layoutEmpty.tvEmptyMessage.text = "您还没有任何支付记录"
            binding.layoutEmpty.ivEmptyIcon.setImageResource(com.sharedparking.android.R.drawable.ic_payment_history_empty)
        }
    }

    private fun showErrorView(show: Boolean, message: String? = null) {
        binding.layoutError.root.visibility = if (show) View.VISIBLE else View.GONE
        if (show && message != null) {
            binding.layoutError.tvErrorMessage.text = message
        }
    }

    companion object {
        const val REQUEST_CODE = 1002
    }
}