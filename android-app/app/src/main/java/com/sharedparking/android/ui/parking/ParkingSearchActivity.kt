package com.sharedparking.android.ui.parking

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharedparking.android.R
import com.sharedparking.android.adapter.ParkingSpotAdapter
import com.sharedparking.android.databinding.ActivityParkingSearchBinding
import com.sharedparking.android.model.ParkingSearchFilters
import com.sharedparking.android.model.ParkingSpot
import com.sharedparking.android.viewmodel.ParkingViewModel

/**
 * 停车位搜索Activity
 * 提供高级搜索和筛选功能
 */
class ParkingSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParkingSearchBinding
    private lateinit var viewModel: ParkingViewModel
    private lateinit var adapter: ParkingSpotAdapter

    // 当前搜索过滤器
    private var currentFilters = ParkingSearchFilters()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParkingSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置工具栏
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[ParkingViewModel::class.java]

        // 初始化UI
        setupUI()

        // 观察数据变化
        observeViewModel()

        // 执行初始搜索（使用默认位置）
        performSearch()
    }

    /**
     * 初始化UI
     */
    private fun setupUI() {
        // 设置RecyclerView
        setupRecyclerView()

        // 搜索框监听
        binding.etSearchKeyword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        // 当前位置按钮
        binding.btnCurrentLocation.setOnClickListener {
            // TODO: 获取当前位置并搜索
            // 暂时使用默认搜索
            performSearch()
        }

        // 高级筛选按钮
        binding.btnAdvancedFilter.setOnClickListener {
            showAdvancedFilterDialog()
        }

        // 下拉刷新
        binding.swipeRefreshLayout.setOnRefreshListener {
            performSearch()
        }

        // 重试按钮
        binding.btnRetry.setOnClickListener {
            performSearch()
        }
    }

    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        adapter = ParkingSpotAdapter(
            onItemClick = { spot ->
                // 跳转到详情页面
                ParkingDetailActivity.start(this, spot.id)
            },
            onFavoriteClick = { spot ->
                toggleFavorite(spot)
            }
        )

        binding.rvParkingSpots.layoutManager = LinearLayoutManager(this)
        binding.rvParkingSpots.adapter = adapter

        // 添加滚动监听实现分页加载
        binding.rvParkingSpots.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                // 当滚动到最后几个项目时加载更多
                if (lastVisibleItemPosition >= totalItemCount - 5) {
                    loadMore()
                }
            }
        })
    }

    /**
     * 观察ViewModel数据变化
     */
    private fun observeViewModel() {
        // 观察搜索状态
        viewModel.searchState.observe(this) { state ->
            when (state) {
                is com.sharedparking.android.viewmodel.ParkingSearchState.Idle -> {
                    // 初始状态
                }
                is com.sharedparking.android.viewmodel.ParkingSearchState.Loading -> {
                    showLoading()
                }
                is com.sharedparking.android.viewmodel.ParkingSearchState.Success -> {
                    showSuccess(state.spots)
                }
                is com.sharedparking.android.viewmodel.ParkingSearchState.Error -> {
                    showError(state.message)
                }
            }
        }

        // 观察当前车位列表变化
        viewModel.currentSpots.observe(this) { spots ->
            // 更新适配器数据
            adapter.updateSpots(spots)
        }
    }

    /**
     * 执行搜索
     */
    private fun performSearch() {
        // 更新搜索关键词
        val keyword = binding.etSearchKeyword.text.toString().trim()
        currentFilters = currentFilters.copy(keyword = if (keyword.isNotEmpty()) keyword else null)

        // 执行搜索
        viewModel.searchParkingSpots(currentFilters)
    }

    /**
     * 加载更多数据
     */
    private fun loadMore() {
        viewModel.loadMoreParkingSpots()
    }

    /**
     * 切换收藏状态
     */
    private fun toggleFavorite(spot: ParkingSpot) {
        if (spot.isFavorite) {
            viewModel.removeFavorite(spot.id)
        } else {
            viewModel.addFavorite(spot.id)
        }
    }

    /**
     * 显示高级筛选对话框
     */
    private fun showAdvancedFilterDialog() {
        // TODO: 实现高级筛选对话框
        // 暂时显示简单提示
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("高级筛选")
            .setMessage("高级筛选功能开发中...")
            .setPositiveButton("确定", null)
            .show()
    }

    /**
     * 显示加载状态
     */
    private fun showLoading() {
        binding.swipeRefreshLayout.isRefreshing = false
        binding.layoutLoading.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.rvParkingSpots.visibility = View.GONE
    }

    /**
     * 显示成功状态
     */
    private fun showSuccess(spots: List<ParkingSpot>) {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutError.visibility = View.GONE

        if (spots.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvParkingSpots.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvParkingSpots.visibility = View.VISIBLE
        }
    }

    /**
     * 显示错误状态
     */
    private fun showError(message: String) {
        binding.layoutLoading.visibility = View.GONE
        binding.rvParkingSpots.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
    }

    /**
     * 处理返回按钮
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        /**
         * 启动Activity
         */
        fun start(activity: AppCompatActivity, initialKeyword: String? = null) {
            val intent = android.content.Intent(activity, ParkingSearchActivity::class.java).apply {
                initialKeyword?.let { putExtra("keyword", it) }
            }
            activity.startActivity(intent)
        }
    }
}