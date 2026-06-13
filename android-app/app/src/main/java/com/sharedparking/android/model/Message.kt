package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName

/**
 * 消息数据模型
 * 映射后端 /api/messages 返回的JSON格式
 */
data class Message(
    val id: Int = 0,
    @SerializedName("sender_id")
    val senderId: Int = 0,
    @SerializedName("receiver_id")
    val receiverId: Int = 0,
    @SerializedName("booking_id")
    val bookingId: Int? = null,
    val subject: String? = null,
    val content: String = "",
    @SerializedName("is_read")
    val isRead: Boolean = false,
    @SerializedName("read_at")
    val readAt: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("sender_username")
    val senderUsername: String? = null,
    @SerializedName("sender_avatar")
    val senderAvatar: String? = null,
    @SerializedName("receiver_username")
    val receiverUsername: String? = null,
    @SerializedName("receiver_avatar")
    val receiverAvatar: String? = null
)
