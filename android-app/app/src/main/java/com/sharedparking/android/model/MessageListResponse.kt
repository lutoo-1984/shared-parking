package com.sharedparking.android.model

import com.google.gson.annotations.SerializedName

/**
 * 消息列表响应
 */
data class MessageListResponse(
    val messages: List<Message> = emptyList(),
    val pagination: Pagination? = null,
    @SerializedName("unread_count")
    val unreadCount: Int = 0
)

/**
 * 未读消息数响应
 */
data class UnreadCountResponse(
    @SerializedName("unread_count")
    val unreadCount: Int = 0
)

/**
 * 发送消息请求
 */
data class SendMessageRequest(
    @SerializedName("receiver_id")
    val receiverId: Int,
    val content: String,
    val subject: String? = null,
    @SerializedName("booking_id")
    val bookingId: Int? = null
)
