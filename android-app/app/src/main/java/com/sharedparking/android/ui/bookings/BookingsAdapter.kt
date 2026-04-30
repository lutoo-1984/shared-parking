package com.sharedparking.android.ui.bookings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharedparking.android.databinding.ItemBookingBinding
import com.sharedparking.android.model.Booking
import com.sharedparking.android.model.BookingStatus

class BookingsAdapter(
    private val onItemClick: (Booking) -> Unit
) : ListAdapter<Booking, BookingsAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookingViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookingViewHolder(
        private val binding: ItemBookingBinding,
        private val onItemClick: (Booking) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            binding.root.setOnClickListener { onItemClick(booking) }

            binding.tvSpotTitle.text = booking.spotTitle ?: "车位"
            binding.tvTime.text = buildString {
                append(booking.startTime ?: "--")
                append(" - ")
                append(booking.endTime ?: "--")
            }
            binding.tvPlateNumber.text = booking.vehiclePlateNumber ?: ""
            binding.tvPrice.text = "¥${String.format("%.2f", booking.totalPrice)}"

            // 状态
            val (text, color) = getStatusStyle(booking.getBookingStatus())
            binding.tvStatus.text = text
            binding.tvStatus.setTextColor(color)
        }

        private fun getStatusStyle(status: BookingStatus): Pair<String, Int> {
            val context = binding.root.context
            return when (status) {
                BookingStatus.PENDING -> "待确认" to context.getColor(com.sharedparking.android.R.color.text_warning)
                BookingStatus.CONFIRMED -> "已确认" to context.getColor(com.sharedparking.android.R.color.text_info)
                BookingStatus.IN_PROGRESS -> "进行中" to context.getColor(com.sharedparking.android.R.color.text_info)
                BookingStatus.COMPLETED -> "已完成" to context.getColor(com.sharedparking.android.R.color.text_success)
                BookingStatus.CANCELLED -> "已取消" to context.getColor(com.sharedparking.android.R.color.text_error)
                BookingStatus.EXPIRED -> "已过期" to context.getColor(com.sharedparking.android.R.color.text_disabled)
            }
        }
    }

    private class BookingDiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Booking, newItem: Booking) = oldItem == newItem
    }
}
