package com.sharedparking.android.ui.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sharedparking.android.databinding.ActivityBookingBinding
import com.sharedparking.android.model.ParkingSpot
import com.sharedparking.android.utils.Resource
import com.sharedparking.android.viewmodel.BookingViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 预订Activity
 * 处理停车位预订功能
 */
class BookingActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SPOT_ID = "extra_spot_id"
        const val EXTRA_SPOT = "extra_spot"
    }

    private lateinit var binding: ActivityBookingBinding
    private lateinit var viewModel: BookingViewModel

    private var spotId: Int = 0
    private var spot: ParkingSpot? = null

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传递的数据
        spotId = intent.getIntExtra(EXTRA_SPOT_ID, 0)
        spot = intent.getSerializableExtra(EXTRA_SPOT) as? ParkingSpot

        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(BookingViewModel::class.java)

        // 设置UI
        setupUI()

        // 观察ViewModel状态
        observeViewModel()

        // 初始化数据
        initData()
    }

    private fun setupUI() {
        // 设置工具栏
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "预订停车位"

        // 时间选择器
        binding.btnSelectStartTime.setOnClickListener { showDateTimePicker(true) }
        binding.btnSelectEndTime.setOnClickListener { showDateTimePicker(false) }

        // 确认预订按钮
        binding.btnConfirmBooking.setOnClickListener { submitBooking() }

        // 车辆信息输入监听
        setupVehicleInfoListeners()

        // 备注信息输入监听
        setupNotesListener()
    }

    private fun initData() {
        // 如果有传递的停车位信息，直接使用
        if (spot != null) {
            viewModel.setSpot(spot!!)
            displaySpotInfo(spot!!)
        } else if (spotId > 0) {
            // 否则根据spotId加载停车位信息
            // 这里需要调用API加载停车位详情
            // 暂时使用占位符
            Toast.makeText(this, "加载停车位信息...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "未获取到停车位信息", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 设置默认时间（当前时间 + 1小时开始，+ 3小时结束）
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, 1)
        val defaultStartTime = calendar.time
        calendar.add(Calendar.HOUR, 2)
        val defaultEndTime = calendar.time

        viewModel.setStartTime(defaultStartTime)
        viewModel.setEndTime(defaultEndTime)

        updateTimeDisplay(defaultStartTime, defaultEndTime)
    }

    private fun displaySpotInfo(spot: ParkingSpot) {
        binding.tvSpotTitle.text = spot.title
        binding.tvSpotAddress.text = spot.address
        binding.tvSpotPrice.text = "¥${String.format("%.2f", spot.pricePerHour)}/小时"
        binding.tvUnitPrice.text = "¥${String.format("%.2f", spot.pricePerHour)}/小时"
    }

    private fun setupVehicleInfoListeners() {
        // 车牌号输入监听
        binding.etPlateNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateVehicleInfo()
            }
        })

        // 车辆品牌输入监听
        binding.etVehicleBrand.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateVehicleInfo()
            }
        })

        // 车辆型号输入监听
        binding.etVehicleModel.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateVehicleInfo()
            }
        })

        // 车辆颜色输入监听
        binding.etVehicleColor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateVehicleInfo()
            }
        })
    }

    private fun setupNotesListener() {
        binding.etNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 备注信息实时更新，不需要特殊处理
            }
        })
    }

    private fun updateVehicleInfo() {
        viewModel.updateVehicleInfo(
            plateNumber = binding.etPlateNumber.text.toString().trim(),
            brand = binding.etVehicleBrand.text.toString().trim(),
            model = binding.etVehicleModel.text.toString().trim(),
            color = binding.etVehicleColor.text.toString().trim()
        )
    }

    private fun showDateTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()

        // 创建日期选择器
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }

                // 创建时间选择器
                val timePicker = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        val selectedDateTime = Calendar.getInstance().apply {
                            set(year, month, dayOfMonth, hourOfDay, minute)
                        }

                        if (isStartTime) {
                            viewModel.setStartTime(selectedDateTime.time)
                            binding.tvStartTime.text = dateTimeFormatter.format(selectedDateTime.time)
                        } else {
                            viewModel.setEndTime(selectedDateTime.time)
                            binding.tvEndTime.text = dateTimeFormatter.format(selectedDateTime.time)
                        }

                        updateTimeDisplay()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // 设置最小日期为当前时间
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    private fun updateTimeDisplay(startTime: Date? = null, endTime: Date? = null) {
        val start = startTime ?: viewModel.startTime.value
        val end = endTime ?: viewModel.endTime.value

        if (start != null) {
            binding.tvStartTime.text = dateTimeFormatter.format(start)
        }

        if (end != null) {
            binding.tvEndTime.text = dateTimeFormatter.format(end)
        }

        // 更新时长显示
        if (start != null && end != null) {
            val durationHours = viewModel.durationHours.value ?: 0.0
            binding.tvDuration.text = "时长: ${String.format("%.1f", durationHours)}小时"
            binding.tvCalculatedDuration.text = "${String.format("%.1f", durationHours)}小时"
        }

        // 更新价格显示
        val totalPrice = viewModel.totalPrice.value ?: 0.0
        binding.tvTotalPrice.text = "¥${String.format("%.2f", totalPrice)}"
        binding.tvBottomTotalPrice.text = "¥${String.format("%.2f", totalPrice)}"
    }

    private fun submitBooking() {
        if (!viewModel.validateForm()) {
            Toast.makeText(this, "请填写完整的预订信息", Toast.LENGTH_SHORT).show()
            return
        }

        val notes = binding.etNotes.text.toString().trim()
        viewModel.createBooking(notes)
    }

    private fun observeViewModel() {
        // 观察价格变化
        viewModel.totalPrice.observe(this) { price ->
            binding.tvTotalPrice.text = "¥${String.format("%.2f", price)}"
            binding.tvBottomTotalPrice.text = "¥${String.format("%.2f", price)}"
        }

        // 观察时长变化
        viewModel.durationHours.observe(this) { duration ->
            binding.tvDuration.text = "时长: ${String.format("%.1f", duration)}小时"
            binding.tvCalculatedDuration.text = "${String.format("%.1f", duration)}小时"
        }

        // 观察创建预订状态
        viewModel.createBookingState.observe(this) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.btnConfirmBooking.isEnabled = false
                    binding.btnConfirmBooking.text = "创建中..."
                }
                is Resource.Success -> {
                    binding.btnConfirmBooking.isEnabled = true
                    binding.btnConfirmBooking.text = "确认预订并支付"

                    val booking = state.data
                    if (booking != null) {
                        Toast.makeText(this, "预订创建成功", Toast.LENGTH_SHORT).show()
                        // 跳转到支付页面
                        navigateToPayment(booking)
                    } else {
                        Toast.makeText(this, "预订数据无效", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    binding.btnConfirmBooking.isEnabled = true
                    binding.btnConfirmBooking.text = "确认预订并支付"

                    Toast.makeText(this, state.message ?: "创建预订失败", Toast.LENGTH_SHORT).show()
                }
                is Resource.Idle -> {
                    // 空闲状态，不做处理
                }
            }
        }

        // 观察可用性状态
        viewModel.availabilityState.observe(this) { state ->
            when (state) {
                is Resource.Success -> {
                    val isAvailable = state.data
                    if (isAvailable != true) {
                        Toast.makeText(this, "该时间段车位已被预订，请选择其他时间", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Error -> {
                    // 可用性检查失败，不阻止用户继续操作
                    // Toast.makeText(this, state.message ?: "检查可用性失败", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // 其他状态不做处理
                }
            }
        }
    }

    private fun navigateToPayment(booking: com.sharedparking.android.model.Booking) {
        val intent = android.content.Intent(this, com.sharedparking.android.ui.payment.PaymentActivity::class.java).apply {
            putExtra(com.sharedparking.android.ui.payment.PaymentActivity.EXTRA_BOOKING_ID, booking.id)
            putExtra(com.sharedparking.android.ui.payment.PaymentActivity.EXTRA_SPOT_TITLE, booking.spotTitle)
            putExtra(com.sharedparking.android.ui.payment.PaymentActivity.EXTRA_TOTAL_PRICE, booking.totalPrice)
            putExtra(com.sharedparking.android.ui.payment.PaymentActivity.EXTRA_START_TIME, booking.startTime)
            putExtra(com.sharedparking.android.ui.payment.PaymentActivity.EXTRA_END_TIME, booking.endTime)
            putExtra(com.sharedparking.android.ui.payment.PaymentActivity.EXTRA_DURATION_HOURS, booking.durationHours)
        }
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}