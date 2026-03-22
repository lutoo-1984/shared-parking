package com.sharedparking.android.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sharedparking.android.databinding.FragmentSearchBinding

/**
 * 搜索Fragment
 * 停车位搜索功能
 */
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    /**
     * 初始化UI
     */
    private fun setupUI() {
        // 设置搜索框点击监听
        binding.searchView.setOnClickListener {
            // 跳转到搜索Activity
            ParkingSearchActivity.start(requireActivity())
        }

        // 设置筛选按钮点击监听
        binding.btnFilter.setOnClickListener {
            // 显示筛选对话框
            showFilterDialog()
        }

        // 设置地图/列表切换
        binding.toggleView.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 地图视图
                binding.rvParkingSpots.visibility = View.GONE
                binding.mapView.visibility = View.VISIBLE
            } else {
                // 列表视图
                binding.rvParkingSpots.visibility = View.VISIBLE
                binding.mapView.visibility = View.GONE
            }
        }
    }

    /**
     * 显示筛选对话框
     */
    private fun showFilterDialog() {
        // 实现筛选对话框
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}