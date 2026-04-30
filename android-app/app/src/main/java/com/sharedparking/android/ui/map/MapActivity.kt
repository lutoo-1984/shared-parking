package com.sharedparking.android.ui.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharedparking.android.adapter.ParkingSpotAdapter
import com.sharedparking.android.databinding.ActivityMapBinding
import com.sharedparking.android.ui.parking.ParkingDetailActivity
import com.sharedparking.android.viewmodel.ParkingSearchState
import com.sharedparking.android.viewmodel.ParkingViewModel
import org.json.JSONArray
import org.json.JSONObject

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding
    private lateinit var viewModel: ParkingViewModel
    private lateinit var adapter: ParkingSpotAdapter
    private var isMapView = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewModel = ViewModelProvider(this)[ParkingViewModel::class.java]

        setupWebView()
        setupRecyclerView()
        setupListeners()
        observeData()

        // 加载附近停车位
        loadSpots()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.mapWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }
        binding.mapWebView.addJavascriptInterface(MapBridge(), "AndroidBridge")
        binding.mapWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                // 页面加载完成后更新标记
                updateMapMarkers()
            }
        }
        binding.mapWebView.loadUrl("file:///android_asset/map.html")
    }

    private fun setupRecyclerView() {
        adapter = ParkingSpotAdapter(
            onItemClick = { spot ->
                ParkingDetailActivity.start(this, spot.id)
            },
            onFavoriteClick = { spot ->
                if (spot.isFavorite) {
                    viewModel.removeFavorite(spot.id)
                } else {
                    viewModel.addFavorite(spot.id)
                }
            }
        )
        binding.rvSpots.layoutManager = LinearLayoutManager(this)
        binding.rvSpots.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabToggleView.setOnClickListener {
            isMapView = !isMapView
            if (isMapView) {
                binding.mapWebView.visibility = View.VISIBLE
                binding.rvSpots.visibility = View.GONE
                binding.fabToggleView.setImageResource(com.sharedparking.android.R.drawable.ic_check)
            } else {
                binding.mapWebView.visibility = View.GONE
                binding.rvSpots.visibility = View.VISIBLE
                binding.fabToggleView.setImageResource(com.sharedparking.android.R.drawable.ic_map)
            }
        }
    }

    private fun observeData() {
        viewModel.searchState.observe(this) { state ->
            when (state) {
                is ParkingSearchState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val spots = state.spots
                    adapter.updateSpots(spots)
                    updateMapMarkers(spots)
                }
                is ParkingSearchState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is ParkingSearchState.Error -> {
                    binding.progressBar.visibility = View.GONE
                }
                else -> {}
            }
        }
    }

    private fun loadSpots() {
        viewModel.searchParkingSpots(
            com.sharedparking.android.model.ParkingSearchFilters(limit = 50)
        )
    }

    private fun updateMapMarkers(spots: List<com.sharedparking.android.model.ParkingSpot>? = null) {
        val spotList = spots ?: viewModel.currentSpots.value ?: emptyList()
        if (spotList.isEmpty()) return

        try {
            val jsonArray = JSONArray()
            for (spot in spotList) {
                if (spot.latitude == 0.0 && spot.longitude == 0.0) continue
                val obj = JSONObject().apply {
                    put("title", spot.title)
                    put("address", spot.address)
                    put("lat", spot.latitude)
                    put("lng", spot.longitude)
                    put("price", spot.pricePerHour)
                    put("id", spot.id)
                }
                jsonArray.put(obj)
            }

            val json = jsonArray.toString()
            binding.mapWebView.post {
                binding.mapWebView.evaluateJavascript(
                    "updateSpotsFromAndroid('${
                        json.replace("'", "\\'")
                            .replace("\n", "")
                    }')", null
                )
            }
        } catch (e: Exception) {
            // 忽略 JSON 错误
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (binding.mapWebView.canGoBack()) {
            binding.mapWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    inner class MapBridge {
        @JavascriptInterface
        fun onSpotClick(spotId: Int) {
            runOnUiThread {
                ParkingDetailActivity.start(this@MapActivity, spotId)
            }
        }
    }
}
