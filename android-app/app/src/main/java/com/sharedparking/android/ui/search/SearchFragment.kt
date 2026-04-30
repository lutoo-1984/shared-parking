package com.sharedparking.android.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharedparking.android.adapter.ParkingSpotAdapter
import com.sharedparking.android.databinding.FragmentSearchBinding
import com.sharedparking.android.model.ParkingSearchFilters
import com.sharedparking.android.model.ParkingSpot
import com.sharedparking.android.ui.parking.AdvancedFilterDialog
import com.sharedparking.android.ui.parking.ParkingDetailActivity
import com.sharedparking.android.viewmodel.ParkingViewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var parkingViewModel: ParkingViewModel
    private lateinit var adapter: ParkingSpotAdapter

    private var currentFilters = ParkingSearchFilters()

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

        parkingViewModel = ViewModelProvider(requireActivity()).get(ParkingViewModel::class.java)

        setupUI()
        setupRecyclerView()
        observeData()
    }

    private fun setupUI() {
        // 搜索框输入监听
        binding.searchView.setOnEditorActionListener { _, _, _ ->
            performSearch()
            true
        }

        // 筛选按钮
        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }

        // 地图/列表切换
        binding.toggleView.setOnClickListener {
            val isChecked = binding.toggleView.isChecked
            binding.toggleView.text = if (isChecked) "列表视图" else "地图视图"
            binding.rvParkingSpots.visibility = if (isChecked) View.GONE else View.VISIBLE
            binding.mapView.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // 下拉刷新
        binding.swipeRefreshLayout.setOnRefreshListener {
            performSearch()
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
        binding.rvParkingSpots.layoutManager = LinearLayoutManager(requireContext())
        binding.rvParkingSpots.adapter = adapter
    }

    private fun observeData() {
        parkingViewModel.currentSpots.observe(viewLifecycleOwner) { spots ->
            adapter.updateSpots(spots)
            binding.tvEmptyResults.visibility = if (spots.isEmpty()) View.VISIBLE else View.GONE
            binding.rvParkingSpots.visibility = if (spots.isEmpty()) View.GONE else View.VISIBLE
        }

        parkingViewModel.searchState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefreshLayout.isRefreshing = state is com.sharedparking.android.viewmodel.ParkingSearchState.Loading
        }
    }

    private fun performSearch() {
        val keyword = binding.searchView.text?.toString()?.trim()
        val filters = currentFilters.copy(keyword = keyword.takeIf { it?.isNotEmpty() == true })
        parkingViewModel.searchParkingSpots(filters)
    }

    private fun showFilterDialog() {
        val dialog = AdvancedFilterDialog.newInstance(currentFilters) { newFilters ->
            currentFilters = newFilters
            performSearch()
        }
        dialog.show(childFragmentManager, "AdvancedFilterDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
