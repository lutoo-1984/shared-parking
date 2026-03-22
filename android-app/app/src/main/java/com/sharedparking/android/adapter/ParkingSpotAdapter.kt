package com.sharedparking.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sharedparking.android.R
import com.sharedparking.android.databinding.ItemParkingSpotBinding
import com.sharedparking.android.model.ParkingSpot
import java.text.NumberFormat
import java.util.Locale

/**
 * 停车位列表适配器
 */
class ParkingSpotAdapter(
    private val onItemClick: (ParkingSpot) -> Unit,
    private val onFavoriteClick: (ParkingSpot) -> Unit
) : RecyclerView.Adapter<ParkingSpotAdapter.ViewHolder>() {

    private var spots: List<ParkingSpot> = emptyList()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)

    /**
     * 更新数据
     */
    fun updateSpots(newSpots: List<ParkingSpot>) {
        spots = newSpots
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemParkingSpotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val spot = spots[position]
        holder.bind(spot)
    }

    override fun getItemCount(): Int = spots.size

    /**
     * ViewHolder
     */
    inner class ViewHolder(
        private val binding: ItemParkingSpotBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(spots[adapterPosition])
                }
            }

            binding.ivFavorite.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onFavoriteClick(spots[adapterPosition])
                }
            }
        }

        /**
         * 绑定数据
         */
        fun bind(spot: ParkingSpot) {
            // 加载图片
            spot.primaryImage?.let { imageUrl ->
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_parking_placeholder)
                    .error(R.drawable.ic_parking_placeholder)
                    .into(binding.ivSpotImage)
            } ?: run {
                binding.ivSpotImage.setImageResource(R.drawable.ic_parking_placeholder)
            }

            // 设置标题和地址
            binding.tvTitle.text = spot.title
            binding.tvAddress.text = spot.address

            // 设置价格
            val priceText = when (spot.priceUnit) {
                "day" -> {
                    val price = spot.pricePerDay ?: spot.pricePerHour * 24
                    "${currencyFormat.format(price)}/天"
                }
                else -> {
                    "${currencyFormat.format(spot.pricePerHour)}/小时"
                }
            }
            binding.tvPrice.text = priceText

            // 设置距离（如果有）
            // TODO: 需要计算实际距离，暂时显示空
            binding.tvDistance.visibility = View.GONE

            // 设置收藏状态
            val favoriteIcon = if (spot.isFavorite) {
                R.drawable.ic_favorite_filled
            } else {
                R.drawable.ic_favorite_border
            }
            binding.ivFavorite.setImageResource(favoriteIcon)

            // 设置设施标签
            updateFacilityChips(spot)

            // 设置评价信息
            spot.avgRating?.let { rating ->
                binding.layoutRating.visibility = View.VISIBLE
                binding.tvRating.text = String.format("%.1f", rating)
                binding.tvReviewCount.text = "(${spot.reviewCount})"
            } ?: run {
                binding.layoutRating.visibility = View.GONE
            }

            // 设置状态标签
            when {
                !spot.isActive -> {
                    binding.chipStatus.text = "已下架"
                    binding.chipStatus.setChipBackgroundColorResource(R.color.error)
                }
                !spot.isApproved -> {
                    binding.chipStatus.text = "待审核"
                    binding.chipStatus.setChipBackgroundColorResource(R.color.warning)
                }
                spot.availableSpots <= 0 -> {
                    binding.chipStatus.text = "已满"
                    binding.chipStatus.setChipBackgroundColorResource(R.color.error)
                }
                else -> {
                    binding.chipStatus.text = "可预订"
                    binding.chipStatus.setChipBackgroundColorResource(R.color.success)
                }
            }
        }

        /**
         * 更新设施标签显示
         */
        private fun updateFacilityChips(spot: ParkingSpot) {
            // 有顶棚
            binding.chipCovered.visibility = if (spot.isCovered) View.VISIBLE else View.GONE

            // 照明
            binding.chipLighting.visibility = if (spot.hasLighting) View.VISIBLE else View.GONE

            // 安保
            binding.chipSecurity.visibility = if (spot.hasSecurity) View.VISIBLE else View.GONE

            // 充电
            binding.chipCharging.visibility = if (spot.hasCharging) View.VISIBLE else View.GONE

            // 24小时
            binding.chip24h.visibility = if (spot.is24hAccess) View.VISIBLE else View.GONE

            // 如果没有设施，隐藏整个组
            val hasFacilities = spot.isCovered || spot.hasLighting || spot.hasSecurity ||
                    spot.hasCharging || spot.is24hAccess
            binding.chipGroupFacilities.visibility = if (hasFacilities) View.VISIBLE else View.GONE
        }
    }
}