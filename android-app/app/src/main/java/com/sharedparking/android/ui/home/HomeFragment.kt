package com.sharedparking.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sharedparking.android.databinding.FragmentHomeBinding
import com.sharedparking.android.viewmodel.ParkingViewModel

/**
 * 首页Fragment
 * 展示推荐停车位、最近搜索、快捷操作等
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var parkingViewModel: ParkingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化ViewModel
        parkingViewModel = ViewModelProvider(requireActivity()).get(ParkingViewModel::class.java)

        // 初始化UI
        setupUI()

        // 观察数据
        observeData()

        // 加载数据
        loadData()
    }

    /**
     * 初始化UI
     */
    private fun setupUI() {
        // 设置刷新监听
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }

        // 设置点击监听
        binding.btnSearchParking.setOnClickListener {
            // 跳转到搜索页面
            // findNavController().navigate(R.id.action_home_to_search)
        }

        binding.btnViewBookings.setOnClickListener {
            // 跳转到预订页面
            // findNavController().navigate(R.id.action_home_to_bookings)
        }

        binding.btnCreateSpot.setOnClickListener {
            // 跳转到创建车位页面
            // findNavController().navigate(R.id.action_home_to_create_spot)
        }
    }

    /**
     * 观察数据变化
     */
    private fun observeData() {
        // 观察推荐停车位
        parkingViewModel.recommendedSpots.observe(viewLifecycleOwner) { spots ->
            // 更新推荐列表
            if (spots.isNotEmpty()) {
                binding.tvEmptyRecommendations.visibility = View.GONE
                binding.rvRecommendations.visibility = View.VISIBLE
                // 设置适配器
            } else {
                binding.tvEmptyRecommendations.visibility = View.VISIBLE
                binding.rvRecommendations.visibility = View.GONE
            }
        }

        // 观察最近搜索
        parkingViewModel.recentSearches.observe(viewLifecycleOwner) { searches ->
            // 更新最近搜索
        }

        // 观察加载状态
        parkingViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        // 加载推荐停车位
        parkingViewModel.loadRecommendedSpots()

        // 加载最近搜索
        parkingViewModel.loadRecentSearches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}