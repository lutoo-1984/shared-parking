package com.sharedparking.android.ui.parking

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharedparking.android.adapter.ParkingSpotAdapter
import com.sharedparking.android.databinding.ActivityMySpotsBinding
import com.sharedparking.android.model.ParkingSpot
import com.sharedparking.android.viewmodel.DeleteSpotState
import com.sharedparking.android.viewmodel.ParkingSearchState
import com.sharedparking.android.viewmodel.ParkingViewModel

class MySpotsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMySpotsBinding
    private lateinit var viewModel: ParkingViewModel
    private lateinit var adapter: ParkingSpotAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySpotsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewModel = ViewModelProvider(this)[ParkingViewModel::class.java]

        setupRecyclerView()
        setupUI()
        observeData()

        viewModel.getMyParkingSpots()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        adapter = ParkingSpotAdapter(
            onItemClick = { spot ->
                ParkingDetailActivity.start(this, spot.id)
            },
            onFavoriteClick = { spot ->
                toggleFavorite(spot)
            }
        )
        binding.rvSpots.layoutManager = LinearLayoutManager(this)
        binding.rvSpots.adapter = adapter
    }

    private fun setupUI() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getMyParkingSpots()
        }

        binding.btnCreateSpot.setOnClickListener {
            navigateToCreateSpot()
        }

        binding.fabCreateSpot.setOnClickListener {
            navigateToCreateSpot()
        }
    }

    private fun observeData() {
        viewModel.mySpotsState.observe(this) { state ->
            binding.swipeRefresh.isRefreshing = false

            when (state) {
                is ParkingSearchState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.emptyState.visibility = View.GONE
                    binding.rvSpots.visibility = View.GONE
                    binding.fabCreateSpot.visibility = View.GONE
                }
                is ParkingSearchState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val spots = state.spots
                    adapter.updateSpots(spots)
                    if (spots.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.rvSpots.visibility = View.GONE
                        binding.fabCreateSpot.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.rvSpots.visibility = View.VISIBLE
                        binding.fabCreateSpot.visibility = View.VISIBLE
                    }
                }
                is ParkingSearchState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.updateSpots(emptyList())
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvSpots.visibility = View.GONE
                    binding.fabCreateSpot.visibility = View.GONE
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        viewModel.deleteSpotState.observe(this) { state ->
            if (state is DeleteSpotState.Success) {
                viewModel.getMyParkingSpots()
            }
        }
    }

    private fun toggleFavorite(spot: ParkingSpot) {
        if (spot.isFavorite) {
            viewModel.removeFavorite(spot.id)
        } else {
            viewModel.addFavorite(spot.id)
        }
    }

    private fun navigateToCreateSpot() {
        startActivity(Intent(this, CreateSpotActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        viewModel.getMyParkingSpots()
    }
}
