package com.sharedparking.android.ui.parking

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sharedparking.android.databinding.ActivityCreateSpotBinding
import com.sharedparking.android.model.CreateParkingSpotRequest
import com.sharedparking.android.viewmodel.CreateSpotState
import com.sharedparking.android.viewmodel.ParkingViewModel

class CreateSpotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateSpotBinding
    private lateinit var viewModel: ParkingViewModel

    companion object {
        private const val REQUEST_LOCATION = 1001
    }

    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateSpotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewModel = ViewModelProvider(this)[ParkingViewModel::class.java]

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.inputLayoutAddress.setEndIconOnClickListener {
            openLocationPicker()
        }

        binding.btnSubmit.setOnClickListener {
            submitSpot()
        }
    }

    private fun observeViewModel() {
        viewModel.createSpotState.observe(this) { state ->
            when (state) {
                is CreateSpotState.Loading -> {
                    binding.btnSubmit.isEnabled = false
                    binding.btnSubmit.text = "发布中..."
                }
                is CreateSpotState.Success -> {
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "发布停车位"
                    Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show()
                    viewModel.resetCreateSpotState()
                    finish()
                }
                is CreateSpotState.Error -> {
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "发布停车位"
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun openLocationPicker() {
        val intent = Intent(this, LocationPickerActivity::class.java)
        startActivityForResult(intent, REQUEST_LOCATION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOCATION && resultCode == RESULT_OK) {
            data?.let {
                selectedLatitude = it.getDoubleExtra(LocationPickerActivity.EXTRA_LATITUDE, 0.0)
                selectedLongitude = it.getDoubleExtra(LocationPickerActivity.EXTRA_LONGITUDE, 0.0)
                val address = it.getStringExtra(LocationPickerActivity.EXTRA_ADDRESS)
                address?.let { addr ->
                    binding.etAddress.setText(addr)
                }
            }
        }
    }

    private fun submitSpot() {
        // 清除之前的错误
        binding.inputLayoutTitle.error = null
        binding.inputLayoutAddress.error = null
        binding.inputLayoutPrice.error = null

        // 验证必填字段
        val title = binding.etTitle.text?.toString()?.trim()
        if (title.isNullOrEmpty()) {
            binding.inputLayoutTitle.error = "请输入车位名称"
            binding.etTitle.requestFocus()
            return
        }

        val address = binding.etAddress.text?.toString()?.trim()
        if (address.isNullOrEmpty()) {
            binding.inputLayoutAddress.error = "请输入地址"
            binding.etAddress.requestFocus()
            return
        }

        val priceStr = binding.etPricePerHour.text?.toString()?.trim()
        if (priceStr.isNullOrEmpty()) {
            binding.inputLayoutPrice.error = "请输入价格"
            binding.etPricePerHour.requestFocus()
            return
        }

        val pricePerHour = priceStr.toDoubleOrNull()
        if (pricePerHour == null || pricePerHour <= 0) {
            binding.inputLayoutPrice.error = "请输入有效的价格"
            binding.etPricePerHour.requestFocus()
            return
        }

        val spotsStr = binding.etAvailableSpots.text?.toString()?.trim()
        val totalSpots = spotsStr?.toIntOrNull() ?: 1

        val maxHeight = binding.etMaxHeight.text?.toString()?.trim()?.toDoubleOrNull()
        val maxWidth = binding.etMaxWidth.text?.toString()?.trim()?.toDoubleOrNull()

        val description = binding.etDescription.text?.toString()?.trim()

        // 如果没有选择位置，使用默认值
        val latitude = selectedLatitude ?: 0.0
        val longitude = selectedLongitude ?: 0.0

        val request = CreateParkingSpotRequest(
            title = title,
            description = description.takeIf { it?.isNotEmpty() == true },
            address = address,
            latitude = latitude,
            longitude = longitude,
            pricePerHour = pricePerHour,
            pricePerDay = pricePerHour * 24,
            maxVehicleHeight = maxHeight,
            maxVehicleWidth = maxWidth,
            availableSpots = totalSpots,
            totalSpots = totalSpots,
            isCovered = binding.cbCovered.isChecked,
            hasLighting = binding.cbLighting.isChecked,
            hasSecurity = binding.cbSecurity.isChecked,
            hasCharging = binding.cbCharging.isChecked,
            hasCctv = binding.cbCctv.isChecked,
            is24hAccess = binding.cb24h.isChecked
        )

        viewModel.createParkingSpot(request)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
