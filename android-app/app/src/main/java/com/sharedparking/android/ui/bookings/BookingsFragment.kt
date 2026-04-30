package com.sharedparking.android.ui.bookings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.sharedparking.android.databinding.FragmentBookingsBinding
import com.sharedparking.android.model.Booking
import com.sharedparking.android.ui.booking.BookingDetailActivity
import com.sharedparking.android.utils.Resource
import com.sharedparking.android.viewmodel.BookingViewModel

class BookingsFragment : Fragment() {

    private var _binding: FragmentBookingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BookingViewModel
    private lateinit var adapter: BookingsAdapter

    private var allBookings: List<Booking> = emptyList()

    // 筛选条件: null=全部, 0=进行中, 1=已完成, 2=已取消
    private var currentFilterIndex = 0

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

        viewModel = ViewModelProvider(this).get(BookingViewModel::class.java)

        setupTabs()
        setupRecyclerView()
        setupRefresh()
        observeData()

        viewModel.getMyBookings()
    }

    private fun setupTabs() {
        binding.tabLayout.removeAllTabs()
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("进行中"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("已完成"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("已取消"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilterIndex = tab?.position ?: 0
                applyFilter()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = BookingsAdapter { booking ->
            val intent = android.content.Intent(requireActivity(), BookingDetailActivity::class.java)
            intent.putExtra("extra_booking_id", booking.id)
            startActivity(intent)
        }
        binding.rvBookings.adapter = adapter
    }

    private fun setupRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getMyBookings()
        }
    }

    private fun observeData() {
        viewModel.myBookingsState.observe(viewLifecycleOwner) { resource ->
            binding.swipeRefreshLayout.isRefreshing = false

            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    allBookings = resource.data ?: emptyList()
                    applyFilter()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    allBookings = emptyList()
                    applyFilter()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun applyFilter() {
        val filtered = when (currentFilterIndex) {
            0 -> allBookings.filter { it.getBookingStatus() in setOf(
                com.sharedparking.android.model.BookingStatus.PENDING,
                com.sharedparking.android.model.BookingStatus.CONFIRMED,
                com.sharedparking.android.model.BookingStatus.IN_PROGRESS
            )}
            1 -> allBookings.filter { it.getBookingStatus() == com.sharedparking.android.model.BookingStatus.COMPLETED }
            2 -> allBookings.filter { it.getBookingStatus() in setOf(
                com.sharedparking.android.model.BookingStatus.CANCELLED,
                com.sharedparking.android.model.BookingStatus.EXPIRED
            )}
            else -> allBookings
        }

        adapter.submitList(filtered)
        binding.emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvBookings.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
