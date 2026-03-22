package com.sharedparking.android.ui.bookings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sharedparking.android.databinding.FragmentBookingsBinding

/**
 * 预订Fragment
 * 展示用户的停车位预订记录
 */
class BookingsFragment : Fragment() {

    private var _binding: FragmentBookingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupTabs()
    }

    /**
     * 初始化UI
     */
    private fun setupUI() {
        // 设置刷新监听
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }
    }

    /**
     * 设置标签页
     */
    private fun setupTabs() {
        // 设置ViewPager和TabLayout
        val tabTitles = arrayOf("进行中", "已完成", "已取消")

        // 创建适配器
        // val adapter = BookingsPagerAdapter(childFragmentManager, lifecycle, tabTitles)
        // binding.viewPager.adapter = adapter
        // binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        // 加载预订数据
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}