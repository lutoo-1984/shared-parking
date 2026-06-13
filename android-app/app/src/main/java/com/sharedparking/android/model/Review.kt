package com.sharedparking.android.model

/**
 * 评价数据模型
 */
data class Review(
    val id: Int = 0,
    val bookingId: Int = 0,
    val userId: Int = 0,
    val spotId: Int = 0,
    val rating: Int = 5,
    val title: String? = null,
    val content: String? = null,
    val ownerReply: String? = null,
    val isVerified: Boolean = false,
    val username: String? = null,
    val avatarUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * 创建评价请求
 */
data class CreateReviewRequest(
    val bookingId: Int,
    val rating: Int,
    val title: String? = null,
    val content: String? = null
)

/**
 * 评价列表响应
 */
data class ReviewListResponse(
    val reviews: List<Review> = emptyList(),
    val ratingStats: RatingStats? = null,
    val pagination: Pagination? = null
)

/**
 * 评分统计
 */
data class RatingStats(
    val average: Double = 0.0,
    val total: Int = 0,
    val distribution: Map<Int, Int> = emptyMap()
)

/**
 * 通用分页数据
 */
data class Pagination(
    val page: Int = 1,
    val limit: Int = 20,
    val total: Int = 0,
    val pages: Int = 0
)
