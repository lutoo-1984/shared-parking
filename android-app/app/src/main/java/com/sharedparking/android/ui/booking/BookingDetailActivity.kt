package com.sharedparking.android.ui.booking

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.sharedparking.android.R
import com.sharedparking.android.databinding.ActivityBookingDetailBinding
import com.sharedparking.android.model.Booking
import com.sharedparking.android.model.BookingStatus
import com.sharedparking.android.utils.Resource
import com.sharedparking.android.viewmodel.BookingViewModel

class BookingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingDetailBinding
    private lateinit var viewModel: BookingViewModel

    private var bookingId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookingId = intent.getIntExtra("extra_booking_id", 0)
        if (bookingId <= 0) {
            Toast.makeText(this, "预订信息无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this).get(BookingViewModel::class.java)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupObservers()
        setupClickListeners()

        viewModel.getBooking(bookingId)
    }

    private fun setupObservers() {
        viewModel.bookingDetailState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // 可以显示加载进度
                }
                is Resource.Success -> {
                    val booking = resource.data
                    if (booking != null) {
                        bindBookingData(booking)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this, resource.message ?: "加载失败", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        viewModel.cancelBookingState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnCancelBooking.isEnabled = false
                    binding.btnCancelBooking.text = "取消中..."
                }
                is Resource.Success -> {
                    binding.btnCancelBooking.isEnabled = false
                    binding.btnCancelBooking.text = "已取消"
                    Toast.makeText(this, "预订已取消", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.btnCancelBooking.isEnabled = true
                    binding.btnCancelBooking.text = "取消预订"
                    Toast.makeText(this, resource.message ?: "取消失败", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun bindBookingData(booking: Booking) {
        binding.tvBookingStatus.text = getStatusText(booking.getBookingStatus())
        binding.tvBookingStatus.setTextColor(getStatusColor(booking.getBookingStatus()))
        binding.tvSpotTitle.text = booking.spotTitle ?: "车位"
        binding.tvSpotAddress.text = booking.spotAddress ?: ""
        binding.tvStartTime.text = booking.startTime ?: "--"
        binding.tvEndTime.text = booking.endTime ?: "--"
        binding.tvDuration.text = "${String.format("%.1f", booking.durationHours)}小时"
        binding.tvPricePerHour.text = "¥${String.format("%.2f", booking.pricePerHour)}/小时"
        binding.tvTotalPrice.text = "¥${String.format("%.2f", booking.totalPrice)}"
        binding.tvPlateNumber.text = booking.vehiclePlateNumber ?: "--"
        binding.tvVehicleBrand.text = booking.vehicleBrand ?: "--"
        binding.tvVehicleColor.text = booking.vehicleColor ?: "--"

        // 支付信息（从booking状态推断）
        val status = booking.getBookingStatus()
        when (status) {
            BookingStatus.PENDING -> {
                binding.tvPaymentStatus.text = "待支付"
                binding.tvPaymentStatus.setTextColor(ContextCompat.getColor(this, R.color.text_warning))
                binding.btnGoPayment.visibility = android.view.View.VISIBLE
            }
            BookingStatus.CONFIRMED -> {
                binding.tvPaymentStatus.text = "已支付"
                binding.tvPaymentStatus.setTextColor(ContextCompat.getColor(this, R.color.text_success))
                binding.btnGoPayment.visibility = android.view.View.GONE
            }
            BookingStatus.COMPLETED -> {
                binding.tvPaymentStatus.text = "已完成"
                binding.tvPaymentStatus.setTextColor(ContextCompat.getColor(this, R.color.text_success))
                binding.btnGoPayment.visibility = android.view.View.GONE
            }
            BookingStatus.CANCELLED -> {
                binding.tvPaymentStatus.text = "已取消"
                binding.tvPaymentStatus.setTextColor(ContextCompat.getColor(this, R.color.text_error))
                binding.btnGoPayment.visibility = android.view.View.GONE
                binding.btnCancelBooking.visibility = android.view.View.GONE
            }
            else -> {
                binding.tvPaymentStatus.text = status.value
                binding.btnGoPayment.visibility = android.view.View.GONE
            }
        }

        // 取消按钮可见性
        if (!booking.canCancel()) {
            binding.btnCancelBooking.visibility = android.view.View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.btnCancelBooking.setOnClickListener {
            viewModel.cancelBooking(bookingId)
        }

        binding.btnGoPayment.setOnClickListener {
            val intent = android.content.Intent(this, com.sharedparking.android.ui.payment.PaymentActivity::class.java).apply {
                putExtra(com.sharedparking.android.ui.payment.PaymentActivity.EXTRA_BOOKING_ID, bookingId)
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getBooking(bookingId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getStatusText(status: BookingStatus): String {
        return when (status) {
            BookingStatus.PENDING -> "待确认"
            BookingStatus.CONFIRMED -> "已确认"
            BookingStatus.IN_PROGRESS -> "进行中"
            BookingStatus.COMPLETED -> "已完成"
            BookingStatus.CANCELLED -> "已取消"
            BookingStatus.EXPIRED -> "已过期"
        }
    }

    private fun getStatusColor(status: BookingStatus): Int {
        return when (status) {
            BookingStatus.PENDING -> ContextCompat.getColor(this, R.color.text_warning)
            BookingStatus.CONFIRMED -> ContextCompat.getColor(this, R.color.text_info)
            BookingStatus.IN_PROGRESS -> ContextCompat.getColor(this, R.color.text_info)
            BookingStatus.COMPLETED -> ContextCompat.getColor(this, R.color.text_success)
            BookingStatus.CANCELLED -> ContextCompat.getColor(this, R.color.text_error)
            BookingStatus.EXPIRED -> ContextCompat.getColor(this, R.color.text_disabled)
        }
    }
}
