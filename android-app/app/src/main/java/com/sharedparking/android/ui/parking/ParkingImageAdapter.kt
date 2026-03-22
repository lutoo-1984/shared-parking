package com.sharedparking.android.ui.parking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sharedparking.android.R
import com.sharedparking.android.databinding.ItemParkingImageBinding

/**
 * 停车位图片轮播适配器
 */
class ParkingImageAdapter : ListAdapter<String, ParkingImageAdapter.ParkingImageViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParkingImageViewHolder {
        val binding = ItemParkingImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ParkingImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParkingImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder
     */
    inner class ParkingImageViewHolder(private val binding: ItemParkingImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String) {
            if (imageUrl == "placeholder") {
                // 显示占位图
                binding.ivParkingImage.setImageResource(R.drawable.ic_parking)
                binding.ivParkingImage.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            } else {
                // 加载网络图片
                binding.ivParkingImage.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_parking)
                    .error(R.drawable.ic_parking)
                    .into(binding.ivParkingImage)
            }
        }
    }

    /**
     * DiffCallback
     */
    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}