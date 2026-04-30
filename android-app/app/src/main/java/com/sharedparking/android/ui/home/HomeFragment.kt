package com.sharedparking.android.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharedparking.android.adapter.ParkingSpotAdapter
import com.sharedparking.android.databinding.FragmentHomeBinding
import com.sharedparking.android.model.ParkingSearchFilters
import com.sharedparking.android.ui.parking.ParkingDetailActivity
import com.sharedparking.android.viewmodel.ParkingViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var parkingViewModel: ParkingViewModel
    private lateinit var adapter: ParkingSpotAdapter

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

        parkingViewModel = ViewModelProvider(requireActivity()).get(ParkingViewModel::class.java)

        setupUI()
        setupRecyclerView()
        observeData()
        loadData()
    }

    private fun setupUI() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }

        binding.btnSearchParking.setOnClickListener {
            val intent = Intent(requireActivity(), com.sharedparking.android.ui.parking.ParkingSearchActivity::class.java)
            startActivity(intent)
        }

        binding.btnMapView.setOnClickListener {
            val intent = Intent(requireActivity(), com.sharedparking.android.ui.map.MapActivity::class.java)
            startActivity(intent)
        }

        binding.btnViewBookings.setOnClickListener {
            // 切换到预订Tab
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                com.sharedparking.android.R.id.bottomNavigationView
            )?.selectedItemId = com.sharedparking.android.R.id.navigation_bookings
        }

        binding.btnCreateSpot.setOnClickListener {
            val intent = Intent(requireActivity(), com.sharedparking.android.ui.parking.CreateSpotActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = ParkingSpotAdapter(
            onItemClick = { spot ->
                ParkingDetailActivity.start(requireActivity() as androidx.appcompat.app.AppCompatActivity, spot.id)
            },
            onFavoriteClick = { spot ->
                if (spot.isFavorite) {
                    parkingViewModel.removeFavorite(spot.id)
                } else {
                    parkingViewModel.addFavorite(spot.id)
                }
            }
        )
        binding.rvRecommendations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecommendations.adapter = adapter
    }

    private fun observeData() {
        parkingViewModel.currentSpots.observe(viewLifecycleOwner) { spots ->
            if (spots.isNotEmpty()) {
                binding.tvEmptyRecommendations.visibility = View.GONE
                binding.rvRecommendations.visibility = View.VISIBLE
                adapter.updateSpots(spots.take(5)) // 首页只显示前5个
            } else {
                binding.tvEmptyRecommendations.visibility = View.VISIBLE
                binding.rvRecommendations.visibility = View.GONE
            }
        }

        parkingViewModel.searchState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefreshLayout.isRefreshing = state is com.sharedparking.android.viewmodel.ParkingSearchState.Loading
        }
    }

    private fun loadData() {
        // 加载推荐停车位（使用默认搜索条件）
        val filters = ParkingSearchFilters(
            sortBy = "rating",
            limit = 10
        )
        parkingViewModel.searchParkingSpots(filters)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
