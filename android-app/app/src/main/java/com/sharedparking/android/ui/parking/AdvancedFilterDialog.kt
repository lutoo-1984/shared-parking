package com.sharedparking.android.ui.parking

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.sharedparking.android.R
import com.sharedparking.android.databinding.DialogAdvancedFilterBinding
import com.sharedparking.android.model.ParkingSearchFilters
import com.sharedparking.android.viewmodel.ParkingViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * 高级筛选对话框
 */
class AdvancedFilterDialog : DialogFragment() {

    private var _binding: DialogAdvancedFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ParkingViewModel
    private var onFilterApplied: ((ParkingSearchFilters) -> Unit)? = null

    // 当前筛选器
    private var currentFilters = ParkingSearchFilters()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)

    companion object {
        fun newInstance(
            currentFilters: ParkingSearchFilters,
            onFilterApplied: (ParkingSearchFilters) -> Unit
        ): AdvancedFilterDialog {
            return AdvancedFilterDialog().apply {
                this.currentFilters = currentFilters
                this.onFilterApplied = onFilterApplied
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_SharedParking_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAdvancedFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化ViewModel
        viewModel = ViewModelProvider(requireActivity())[ParkingViewModel::class.java]

        // 设置对话框标题
        dialog?.setTitle("高级筛选")

        // 初始化UI
        setupUI()

        // 设置按钮事件
        setupButtons()
    }

    /**
     * 初始化UI
     */
    private fun setupUI() {
        // 价格范围
        currentFilters.minPrice?.let {
            binding.etMinPrice.setText(it.toString())
        }
        currentFilters.maxPrice?.let {
            binding.etMaxPrice.setText(it.toString())
        }

        // 设施筛选
        binding.switchCovered.isChecked = currentFilters.isCovered ?: false
        binding.switchLighting.isChecked = currentFilters.hasLighting ?: false
        binding.switchSecurity.isChecked = currentFilters.hasSecurity ?: false
        binding.switchCharging.isChecked = currentFilters.hasCharging ?: false
        binding.switchCctv.isChecked = currentFilters.hasCctv ?: false
        binding.switch24h.isChecked = currentFilters.is24hAccess ?: false

        // 车辆尺寸
        currentFilters.maxHeight?.let {
            binding.etMaxHeight.setText(it.toString())
        }
        currentFilters.maxWidth?.let {
            binding.etMaxWidth.setText(it.toString())
        }

        // 排序方式
        when (currentFilters.sortBy) {
            "price_asc" -> binding.radioPriceAsc.isChecked = true
            "price_desc" -> binding.radioPriceDesc.isChecked = true
            "rating" -> binding.radioRating.isChecked = true
            "distance" -> binding.radioDistance.isChecked = true
            else -> binding.radioDefault.isChecked = true
        }

        // 搜索半径
        currentFilters.radius?.let {
            binding.sliderRadius.value = it.toFloat()
        }
    }

    /**
     * 设置按钮事件
     */
    private fun setupButtons() {
        // 重置按钮
        binding.btnReset.setOnClickListener {
            resetFilters()
        }

        // 取消按钮
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // 应用按钮
        binding.btnApply.setOnClickListener {
            applyFilters()
        }
    }

    /**
     * 重置筛选器
     */
    private fun resetFilters() {
        currentFilters = ParkingSearchFilters()
        setupUI()
        Toast.makeText(requireContext(), "筛选器已重置", Toast.LENGTH_SHORT).show()
    }

    /**
     * 应用筛选器
     */
    private fun applyFilters() {
        try {
            // 价格范围
            val minPriceText = binding.etMinPrice.text.toString()
            val maxPriceText = binding.etMaxPrice.text.toString()

            val minPrice = if (minPriceText.isNotEmpty()) minPriceText.toDouble() else null
            val maxPrice = if (maxPriceText.isNotEmpty()) maxPriceText.toDouble() else null

            // 验证价格范围
            if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
                Toast.makeText(requireContext(), "最低价格不能高于最高价格", Toast.LENGTH_SHORT).show()
                return
            }

            // 车辆尺寸
            val maxHeightText = binding.etMaxHeight.text.toString()
            val maxWidthText = binding.etMaxWidth.text.toString()

            val maxHeight = if (maxHeightText.isNotEmpty()) maxHeightText.toDouble() else null
            val maxWidth = if (maxWidthText.isNotEmpty()) maxWidthText.toDouble() else null

            // 排序方式
            val sortBy = when {
                binding.radioPriceAsc.isChecked -> "price_asc"
                binding.radioPriceDesc.isChecked -> "price_desc"
                binding.radioRating.isChecked -> "rating"
                binding.radioDistance.isChecked -> "distance"
                else -> null
            }

            // 更新筛选器
            val updatedFilters = currentFilters.copy(
                minPrice = minPrice,
                maxPrice = maxPrice,
                isCovered = if (binding.switchCovered.isChecked) true else null,
                hasLighting = if (binding.switchLighting.isChecked) true else null,
                hasSecurity = if (binding.switchSecurity.isChecked) true else null,
                hasCharging = if (binding.switchCharging.isChecked) true else null,
                hasCctv = if (binding.switchCctv.isChecked) true else null,
                is24hAccess = if (binding.switch24h.isChecked) true else null,
                maxHeight = maxHeight,
                maxWidth = maxWidth,
                sortBy = sortBy,
                radius = binding.sliderRadius.value.toDouble()
            )

            // 回调应用筛选器
            onFilterApplied?.invoke(updatedFilters)
            dismiss()

        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "请输入有效的数字", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}