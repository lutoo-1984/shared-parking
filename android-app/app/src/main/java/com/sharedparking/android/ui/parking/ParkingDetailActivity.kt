package com.sharedparking.android.ui.parking

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.sharedparking.android.R
import com.sharedparking.android.databinding.ActivityParkingDetailBinding
import com.sharedparking.android.model.ParkingSpot
import com.sharedparking.android.model.SpotImage
import com.sharedparking.android.viewmodel.ParkingViewModel
import com.sharedparking.android.viewmodel.ParkingDetailState

/**
 * 停车位详情Activity
 * 展示停车位详细信息、图片、设施、车主信息等
 */
class ParkingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParkingDetailBinding
    private lateinit var viewModel: ParkingViewModel

    // 图片轮播适配器
    private lateinit var imageAdapter: ParkingImageAdapter
    private var currentSpotId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParkingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[ParkingViewModel::class.java]

        // 获取传递的车位ID
        currentSpotId = intent.getIntExtra("spot_id", -1)
        if (currentSpotId == -1) {
            finish()
            return
        }

        // 初始化UI
        setupUI()

        // 观察数据变化
        observeViewModel()

        // 加载车位详情
        loadSpotDetails()
    }

    /**
     * 初始化UI
     */
    private fun setupUI() {
        // 设置工具栏
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "停车位详情"

        // 返回按钮点击事件
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        // 收藏按钮点击事件
        binding.ivFavorite.setOnClickListener {
            toggleFavorite()
        }

        // 联系车主按钮
        binding.btnContactOwner.setOnClickListener {
            // TODO: 跳转到消息页面
        }

        // 立即预订按钮
        binding.btnBookNow.setOnClickListener {
            // 跳转到预订页面
            startBookingActivity()
        }

        // 设置图片轮播适配器
        imageAdapter = ParkingImageAdapter()
        binding.viewPager.adapter = imageAdapter

        // 绑定TabLayout和ViewPager2
        binding.tabLayout.apply {
            // ViewPager2需要使用TabLayoutMediator
            TabLayoutMediator(this, binding.viewPager) { tab, position ->
                // 可以设置tab的文本或图标，这里留空
            }.attach()
            setSelectedTabIndicator(null)
        }
    }

    /**
     * 观察ViewModel数据变化
     */
    private fun observeViewModel() {
        // 观察车位详情状态
        viewModel.spotDetailState.observe(this) { state ->
            when (state) {
                is ParkingDetailState.Loading -> {
                    showLoading()
                }
                is ParkingDetailState.Success -> {
                    showSuccess(state.spot)
                }
                is ParkingDetailState.Error -> {
                    showError(state.message)
                }
                else -> {}
            }
        }

        // 观察收藏状态
        viewModel.favoriteState.observe(this) { state ->
            when (state) {
                is com.sharedparking.android.viewmodel.FavoriteState.Loading -> {
                    // 收藏操作中
                }
                is com.sharedparking.android.viewmodel.FavoriteState.Success -> {
                    // 更新收藏按钮状态
                    updateFavoriteButton(state.isFavorite)
                }
                else -> {}
            }
        }
    }

    /**
     * 加载车位详情
     */
    private fun loadSpotDetails() {
        viewModel.getParkingSpot(currentSpotId)
    }

    /**
     * 显示加载状态
     */
    private fun showLoading() {
        binding.layoutLoading.visibility = View.VISIBLE
        binding.layoutContent.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    /**
     * 显示成功状态并填充数据
     */
    private fun showSuccess(spot: ParkingSpot) {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutContent.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE

        // 填充数据
        bindSpotData(spot)
    }

    /**
     * 显示错误状态
     */
    private fun showError(message: String) {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutContent.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message

        // 重试按钮
        binding.btnRetry.setOnClickListener {
            loadSpotDetails()
        }
    }

    /**
     * 绑定车位数据到UI
     */
    private fun bindSpotData(spot: ParkingSpot) {
        // 标题和价格
        binding.tvTitle.text = spot.title
        binding.tvPrice.text = getString(R.string.price_per_hour, spot.pricePerHour)
        binding.tvFinalPrice.text = getString(R.string.price_per_hour, spot.pricePerHour)

        // 地址
        binding.tvAddress.text = spot.address

        // 详情描述
        binding.tvDescription.text = spot.description ?: "暂无描述"

        // 车位规格
        binding.tvMaxHeight.text = "${spot.maxVehicleHeight ?: 2.0}米"
        binding.tvMaxWidth.text = "${spot.maxVehicleWidth ?: 2.5}米"
        binding.tvAvailableSpots.text = "${spot.availableSpots}/${spot.totalSpots}"

        // 设施标签
        updateFacilityChips(spot)

        // 车主信息
        binding.tvOwnerUsername.text = spot.ownerUsername ?: "未知"
        binding.tvOwnerRating.text = String.format("%.1f", spot.avgRating ?: 0.0)
        binding.tvOwnerReviewCount.text = getString(R.string.review_count_template, spot.reviewCount)

        // 加载车主头像
        spot.ownerAvatar?.let { avatarUrl ->
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_person)
                .into(binding.ivOwnerAvatar)
        }

        // 更新收藏按钮状态
        updateFavoriteButton(spot.isFavorite)

        // 更新图片轮播
        updateImagePager(spot.images)

        // 如果有原价显示原价，否则隐藏
        // 注意：ParkingSpot模型中没有originalPricePerHour字段，这里使用pricePerDay作为替代
        spot.pricePerDay?.let { pricePerDay ->
            if (pricePerDay > spot.pricePerHour * 24) {
                binding.tvOriginalPrice.text = getString(R.string.price_per_hour, pricePerDay / 24)
                binding.tvOriginalPrice.visibility = View.VISIBLE
            } else {
                binding.tvOriginalPrice.visibility = View.GONE
            }
        } ?: run {
            binding.tvOriginalPrice.visibility = View.GONE
        }
    }

    /**
     * 更新设施标签
     */
    private fun updateFacilityChips(spot: ParkingSpot) {
        binding.chipCovered.isChecked = spot.isCovered
        binding.chipLighting.isChecked = spot.hasLighting
        binding.chipSecurity.isChecked = spot.hasSecurity
        binding.chipCharging.isChecked = spot.hasCharging
        binding.chip24h.isChecked = spot.is24hAccess
        binding.chipCctv.isChecked = spot.hasCctv
    }

    /**
     * 更新图片轮播
     */
    private fun updateImagePager(images: List<SpotImage>) {
        val imageUrls = images.map { it.imageUrl }
        imageAdapter.submitList(imageUrls)
        if (imageUrls.isEmpty()) {
            // 如果没有图片，显示占位图
            imageAdapter.submitList(listOf("placeholder"))
        }
    }

    /**
     * 更新收藏按钮状态
     */
    private fun updateFavoriteButton(isFavorite: Boolean) {
        if (isFavorite) {
            binding.ivFavorite.setImageResource(R.drawable.ic_favorite_filled)
            binding.ivFavorite.contentDescription = "已收藏"
        } else {
            binding.ivFavorite.setImageResource(R.drawable.ic_favorite_border)
            binding.ivFavorite.contentDescription = "未收藏"
        }
    }

    /**
     * 切换收藏状态
     */
    private fun toggleFavorite() {
        if (currentSpotId != -1) {
            // 获取当前收藏按钮的状态
            val isCurrentlyFavorite = binding.ivFavorite.contentDescription == "已收藏"
            if (isCurrentlyFavorite) {
                viewModel.removeFavorite(currentSpotId)
            } else {
                viewModel.addFavorite(currentSpotId)
            }
        }
    }

    /**
     * 跳转到预订页面
     */
    private fun startBookingActivity() {
        val state = viewModel.spotDetailState.value
        if (state is ParkingDetailState.Success && state.spot != null) {
            val spot = state.spot
            // 跳转到BookingActivity - 只传递ID，不传递整个对象
            val intent = android.content.Intent(this, com.sharedparking.android.ui.booking.BookingActivity::class.java).apply {
                putExtra(com.sharedparking.android.ui.booking.BookingActivity.EXTRA_SPOT_ID, spot.id)
            }
            startActivity(intent)
        } else if (currentSpotId != -1) {
            // 如果没有停车位对象，只传递ID
            val intent = android.content.Intent(this, com.sharedparking.android.ui.booking.BookingActivity::class.java).apply {
                putExtra(com.sharedparking.android.ui.booking.BookingActivity.EXTRA_SPOT_ID, currentSpotId)
            }
            startActivity(intent)
        }
    }

    /**
     * 处理返回按钮
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        // 只在需要时刷新收藏状态
        viewModel.resetFavoriteState()
    }

    companion object {
        /**
         * 启动Activity
         */
        fun start(activity: AppCompatActivity, spotId: Int) {
            val intent = android.content.Intent(activity, ParkingDetailActivity::class.java).apply {
                putExtra("spot_id", spotId)
            }
            activity.startActivity(intent)
        }
    }
}