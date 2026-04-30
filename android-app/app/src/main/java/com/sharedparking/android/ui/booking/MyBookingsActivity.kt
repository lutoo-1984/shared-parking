package com.sharedparking.android.ui.booking

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharedparking.android.databinding.ActivityMyBookingsBinding
import com.sharedparking.android.model.Booking
import com.sharedparking.android.ui.bookings.BookingsAdapter
import com.sharedparking.android.utils.Resource
import com.sharedparking.android.viewmodel.BookingViewModel

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyBookingsBinding
    private lateinit var viewModel: BookingViewModel
    private lateinit var adapter: BookingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewModel = ViewModelProvider(this)[BookingViewModel::class.java]

        setupRecyclerView()
        setupRefresh()
        observeData()

        viewModel.getMyBookings()
    }

    private fun setupRecyclerView() {
        adapter = BookingsAdapter { booking ->
            val intent = Intent(this, BookingDetailActivity::class.java).apply {
                putExtra("extra_booking_id", booking.id)
            }
            startActivity(intent)
        }
        binding.rvBookings.layoutManager = LinearLayoutManager(this)
        binding.rvBookings.adapter = adapter
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getMyBookings()
        }
    }

    private fun observeData() {
        viewModel.myBookingsState.observe(this) { resource ->
            binding.swipeRefresh.isRefreshing = false

            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.emptyState.visibility = View.GONE
                    binding.rvBookings.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val bookings = resource.data ?: emptyList()
                    adapter.submitList(bookings)
                    if (bookings.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.rvBookings.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.rvBookings.visibility = View.VISIBLE
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.submitList(emptyList())
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvBookings.visibility = View.GONE
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
