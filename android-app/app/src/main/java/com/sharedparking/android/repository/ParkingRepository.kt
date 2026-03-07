package com.sharedparking.android.repository

import com.sharedparking.android.model.*
import com.sharedparking.android.network.ApiClientBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 停车位仓库类
 * 处理停车位的搜索、创建、更新、删除等操作
 */
class ParkingRepository {

    private val apiService by lazy {
        ApiClientBuilder.getApiService()
    }

    // ===== 公开方法 =====

    /**
     * 搜索停车位
     */
    suspend fun searchParkingSpots(filters: ParkingSearchFilters): Result<ParkingSearchResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // 将过滤器转换为查询参数
                val queryParams = mutableMapOf<String, String>()

                filters.latitude?.let { queryParams["latitude"] = it.toString() }
                filters.longitude?.let { queryParams["longitude"] = it.toString() }
                filters.radius?.let { queryParams["radius"] = it.toString() }
                filters.minPrice?.let { queryParams["min_price"] = it.toString() }
                filters.maxPrice?.let { queryParams["max_price"] = it.toString() }
                filters.keyword?.let { queryParams["keyword"] = it }
                filters.startTime?.let { queryParams["start_time"] = it }
                filters.endTime?.let { queryParams["end_time"] = it }
                filters.isCovered?.let { queryParams["is_covered"] = if (it) "1" else "0" }
                filters.hasLighting?.let { queryParams["has_lighting"] = if (it) "1" else "0" }
                filters.hasSecurity?.let { queryParams["has_security"] = if (it) "1" else "0" }
                filters.hasCharging?.let { queryParams["has_charging"] = if (it) "1" else "0" }
                filters.hasCctv?.let { queryParams["has_cctv"] = if (it) "1" else "0" }
                filters.is24hAccess?.let { queryParams["is_24h_access"] = if (it) "1" else "0" }
                filters.maxHeight?.let { queryParams["max_height"] = it.toString() }
                filters.maxWidth?.let { queryParams["max_width"] = it.toString() }
                filters.sortBy?.let { queryParams["sort_by"] = it }
                queryParams["page"] = filters.page.toString()
                queryParams["limit"] = filters.limit.toString()

                val response = apiService.searchParkingSpots(queryParams)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.success(body.data)
                    } else {
                        val error = body?.error ?: throw Exception("搜索停车位失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取停车位详情
     */
    suspend fun getParkingSpot(id: Int): Result<ParkingSpot> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getParkingSpot(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.success(body.data)
                    } else {
                        val error = body?.error ?: throw Exception("获取停车位详情失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 创建停车位
     */
    suspend fun createParkingSpot(request: CreateParkingSpotRequest): Result<ParkingSpot> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createParkingSpot(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.success(body.data)
                    } else {
                        val error = body?.error ?: throw Exception("创建停车位失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 更新停车位
     */
    suspend fun updateParkingSpot(id: Int, request: CreateParkingSpotRequest): Result<ParkingSpot> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateParkingSpot(id, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.success(body.data)
                    } else {
                        val error = body?.error ?: throw Exception("更新停车位失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 删除停车位
     */
    suspend fun deleteParkingSpot(id: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteParkingSpot(id)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.success(true)
                    } else {
                        val error = body?.error ?: throw Exception("删除停车位失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取我的车位
     */
    suspend fun getMyParkingSpots(page: Int = 1, limit: Int = 20): Result<ParkingSearchResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyParkingSpots(page, limit)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.success(body.data)
                    } else {
                        val error = body?.error ?: throw Exception("获取我的车位失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 检查车位可用性
     */
    suspend fun checkAvailability(
        spotId: Int,
        startTime: String,
        endTime: String
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.checkAvailability(spotId, startTime, endTime)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val isAvailable = body.data["is_available"] as? Boolean
                        Result.success(isAvailable ?: false)
                    } else {
                        val error = body?.error ?: throw Exception("检查可用性失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 添加收藏
     */
    suspend fun addFavorite(spotId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.addFavorite(spotId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.success(true)
                    } else {
                        val error = body?.error ?: throw Exception("添加收藏失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 移除收藏
     */
    suspend fun removeFavorite(spotId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.removeFavorite(spotId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.success(true)
                    } else {
                        val error = body?.error ?: throw Exception("移除收藏失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取收藏列表
     */
    suspend fun getFavorites(page: Int = 1, limit: Int = 20): Result<ParkingSearchResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFavorites(page, limit)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.success(body.data)
                    } else {
                        val error = body?.error ?: throw Exception("获取收藏列表失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 创建预订
     */
    suspend fun createBooking(
        spotId: Int,
        vehiclePlateNumber: String? = null,
        startTime: String,
        endTime: String
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val request = mutableMapOf<String, Any>(
                    "spot_id" to spotId,
                    "start_time" to startTime,
                    "end_time" to endTime
                )
                vehiclePlateNumber?.let { request["vehicle_plate_number"] = it }

                val response = apiService.createBooking(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.success(true)
                    } else {
                        val error = body?.error ?: throw Exception("创建预订失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取我的预订
     */
    suspend fun getMyBookings(page: Int = 1, limit: Int = 20): Result<Any> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyBookings(page, limit)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.success(body.data ?: true)
                    } else {
                        val error = body?.error ?: throw Exception("获取预订列表失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 取消预订
     */
    suspend fun cancelBooking(bookingId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.cancelBooking(bookingId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.success(true)
                    } else {
                        val error = body?.error ?: throw Exception("取消预订失败")
                        Result.failure(Exception(error.message))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}