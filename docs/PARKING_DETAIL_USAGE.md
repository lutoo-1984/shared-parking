# 停车位详情功能使用指南

## 从搜索结果跳转到详情页面

### 1. 在ParkingSearchActivity中
在`ParkingSearchActivity.kt`的`setupRecyclerView()`方法中，已经设置了点击事件：

```kotlin
private fun setupRecyclerView() {
    adapter = ParkingSpotAdapter(
        onItemClick = { spot ->
            // 跳转到详情页面
            ParkingDetailActivity.start(this, spot.id)
        },
        onFavoriteClick = { spot ->
            // 处理收藏点击
            toggleFavorite(spot)
        }
    )
    // ... 其他设置
}
```

### 2. 启动详情Activity
使用`ParkingDetailActivity.start()`静态方法启动详情页面：

```kotlin
// 从任何Activity或Fragment中启动
ParkingDetailActivity.start(activity, spotId)
```

## 数据传递

### 传递参数
- **spot_id**: 停车位ID（Int类型）
- 通过Intent的extra传递

### 接收参数
在`ParkingDetailActivity.onCreate()`中接收参数：

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ...

    // 获取传递的车位ID
    currentSpotId = intent.getIntExtra("spot_id", -1)
    if (currentSpotId == -1) {
        finish()
        return
    }

    // 加载车位详情
    loadSpotDetails()
}
```

## API调用流程

### 1. 获取停车位详情
```kotlin
private fun loadSpotDetails() {
    viewModel.getParkingSpot(currentSpotId)
}
```

### 2. 观察数据状态
```kotlin
private fun observeViewModel() {
    // 观察车位详情状态
    viewModel.spotDetailState.observe(this) { state ->
        when (state) {
            is ParkingDetailState.Loading -> showLoading()
            is ParkingDetailState.Success -> showSuccess(state.spot)
            is ParkingDetailState.Error -> showError(state.message)
            else -> {}
        }
    }

    // 观察收藏状态
    viewModel.favoriteState.observe(this) { state ->
        when (state) {
            is FavoriteState.Success -> updateFavoriteButton(state.isFavorite)
            else -> {}
        }
    }
}
```

## 数据绑定示例

### 绑定停车位数据
```kotlin
private fun bindSpotData(spot: ParkingSpot) {
    // 标题和价格
    binding.tvTitle.text = spot.title
    binding.tvPrice.text = getString(R.string.price_per_hour, spot.pricePerHour)

    // 地址
    binding.tvAddress.text = spot.address

    // 详情描述
    binding.tvDescription.text = spot.description ?: "暂无描述"

    // 车位规格
    binding.tvMaxHeight.text = "${spot.maxVehicleHeight ?: 2.0}米"
    binding.tvMaxWidth.text = "${spot.maxVehicleWidth ?: 2.5}米"
    binding.tvAvailableSpots.text = "${spot.availableSpots}/${spot.totalSpots}"

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
}
```

## 用户交互

### 1. 收藏功能
```kotlin
// 收藏按钮点击事件
binding.ivFavorite.setOnClickListener {
    toggleFavorite()
}

// 切换收藏状态
private fun toggleFavorite() {
    if (currentSpotId != -1) {
        val isCurrentlyFavorite = binding.ivFavorite.contentDescription == "已收藏"
        if (isCurrentlyFavorite) {
            viewModel.removeFavorite(currentSpotId)
        } else {
            viewModel.addFavorite(currentSpotId)
        }
    }
}
```

### 2. 立即预订
```kotlin
// 立即预订按钮点击事件
binding.btnBookNow.setOnClickListener {
    startBookingActivity()
}

// 跳转到预订页面
private fun startBookingActivity() {
    if (currentSpotId != -1) {
        val intent = Intent(this, BookingActivity::class.java).apply {
            putExtra("spot_id", currentSpotId)
        }
        startActivity(intent)
    }
}
```

## 状态管理

### 1. 加载状态
```kotlin
private fun showLoading() {
    binding.layoutLoading.visibility = View.VISIBLE
    binding.layoutContent.visibility = View.GONE
    binding.layoutError.visibility = View.GONE
}
```

### 2. 成功状态
```kotlin
private fun showSuccess(spot: ParkingSpot) {
    binding.layoutLoading.visibility = View.GONE
    binding.layoutContent.visibility = View.VISIBLE
    binding.layoutError.visibility = View.GONE
    bindSpotData(spot)
}
```

### 3. 错误状态
```kotlin
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
```

## 布局结构

### 主要组件
1. **CollapsingToolbarLayout**: 折叠工具栏，包含图片轮播
2. **ViewPager2**: 图片轮播组件
3. **TabLayout**: 图片轮播指示器
4. **NestedScrollView**: 可滚动的内容区域
5. **MaterialCardView**: 信息卡片容器
6. **ChipGroup**: 设施标签组
7. **Bottom App Bar**: 底部操作栏

### 卡片内容
1. **基本信息卡片**: 标题、价格、地址、设施标签
2. **详情描述卡片**: 停车位详细描述
3. **车位规格卡片**: 最大高度、最大宽度、可用车位数
4. **车主信息卡片**: 用户名、评分、评价数量、头像

## 自定义样式

### 1. 芯片样式
```xml
<style name="Widget.SharedParking.Chip.Filter" parent="Widget.SharedParking.Chip">
    <item name="chipBackgroundColor">@color/transparent</item>
    <item name="chipStrokeColor">@color/border</item>
    <item name="chipStrokeWidth">1dp</item>
    <item name="android:textColor">@color/text_secondary</item>
    <item name="checkedIcon">@drawable/ic_check</item>
    <item name="checkedIconVisible">true</item>
    <item name="checkable">true</item>
</style>
```

### 2. 删除线文本样式
```xml
<style name="Text.Body.Small.Strikethrough" parent="Text.Body.Small">
    <item name="android:textStyle">normal</item>
    <item name="android:strikethroughText">true</item>
</style>
```

## 注意事项

### 1. 权限要求
- 网络权限：用于加载图片和数据
- 存储权限：如果需要保存图片到本地

### 2. 性能优化
- 使用Glide进行图片加载和缓存
- 使用ViewBinding减少findViewById调用
- 合理管理生命周期观察者

### 3. 错误处理
- 网络错误：显示友好的错误提示和重试按钮
- 数据错误：验证数据格式，提供默认值
- 图片加载错误：显示占位图

### 4. 用户体验
- 提供加载状态反馈
- 支持下拉刷新（如果需要）
- 保持一致的交互反馈
- 适配不同屏幕尺寸