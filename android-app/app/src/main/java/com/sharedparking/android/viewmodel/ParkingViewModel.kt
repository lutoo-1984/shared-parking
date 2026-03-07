package com.sharedparking.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sharedparking.android.model.*
import com.sharedparking.android.repository.ParkingRepository
import kotlinx.coroutines.launch

/**
 * 停车位相关的ViewModel
 */
class ParkingViewModel(application: Application) : AndroidViewModel(application) {

    private val parkingRepository = ParkingRepository()

    // 搜索状态
    private val _searchState = MutableLiveData<ParkingSearchState>()
    val searchState: LiveData<ParkingSearchState> = _searchState

    // 车位详情状态
    private val _spotDetailState = MutableLiveData<ParkingDetailState>()
    val spotDetailState: LiveData<ParkingDetailState> = _spotDetailState

    // 我的车位状态
    private val _mySpotsState = MutableLiveData<ParkingSearchState>()
    val mySpotsState: LiveData<ParkingSearchState> = _mySpotsState

    // 收藏状态
    private val _favoriteState = MutableLiveData<FavoriteState>()
    val favoriteState: LiveData<FavoriteState> = _favoriteState

    // 收藏列表状态
    private val _favoritesState = MutableLiveData<ParkingSearchState>()
    val favoritesState: LiveData<ParkingSearchState> = _favoritesState

    // 创建车位状态
    private val _createSpotState = MutableLiveData<CreateSpotState>()
    val createSpotState: LiveData<CreateSpotState> = _createSpotState

    // 更新车位状态
    private val _updateSpotState = MutableLiveData<CreateSpotState>()
    val updateSpotState: LiveData<CreateSpotState> = _updateSpotState

    // 删除车位状态
    private val _deleteSpotState = MutableLiveData<DeleteSpotState>()
    val deleteSpotState: LiveData<DeleteSpotState> = _deleteSpotState

    // 可用性检查状态
    private val _availabilityState = MutableLiveData<AvailabilityState>()
    val availabilityState: LiveData<AvailabilityState> = _availabilityState

    // 当前搜索的车位列表
    private val _currentSpots = MutableLiveData<List<ParkingSpot>>()
    val currentSpots: LiveData<List<ParkingSpot>> = _currentSpots

    // 当前搜索的分页信息
    private val _currentPagination = MutableLiveData<Pagination?>()
    val currentPagination: LiveData<Pagination?> = _currentPagination

    // 当前页面
    private var currentPage = 1
    private var currentFilters: ParkingSearchFilters? = null

    /**
     * 搜索停车位
     */
    fun searchParkingSpots(filters: ParkingSearchFilters) {
        viewModelScope.launch {
            _searchState.value = ParkingSearchState.Loading
            try {
                currentFilters = filters
                currentPage = filters.page

                val result = parkingRepository.searchParkingSpots(filters)
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    _currentSpots.value = response?.spots ?: emptyList()
                    _currentPagination.value = response?.pagination
                    _searchState.value = ParkingSearchState.Success(response?.spots ?: emptyList())
                } else {
                    _searchState.value = ParkingSearchState.Error(result.exceptionOrNull()?.message ?: "搜索失败")
                }
            } catch (e: Exception) {
                _searchState.value = ParkingSearchState.Error(e.message ?: "搜索异常")
            }
        }
    }

    /**
     * 加载更多车位（分页）
     */
    fun loadMoreParkingSpots() {
        viewModelScope.launch {
            val filters = currentFilters ?: return@launch
            val pagination = currentPagination.value ?: return@launch

            // 检查是否还有更多页
            if (currentPage >= pagination.pages) {
                return@launch
            }

            // 加载下一页
            val nextPage = currentPage + 1
            val nextFilters = filters.copy(page = nextPage)

            try {
                val result = parkingRepository.searchParkingSpots(nextFilters)
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    val currentList = _currentSpots.value ?: emptyList()
                    val newList = currentList + (response?.spots ?: emptyList())

                    _currentSpots.value = newList
                    _currentPagination.value = response?.pagination
                    currentPage = nextPage
                }
            } catch (e: Exception) {
                // 忽略错误，不更新状态
            }
        }
    }

    /**
     * 获取停车位详情
     */
    fun getParkingSpot(id: Int) {
        viewModelScope.launch {
            _spotDetailState.value = ParkingDetailState.Loading
            try {
                val result = parkingRepository.getParkingSpot(id)
                if (result.isSuccess) {
                    _spotDetailState.value = ParkingDetailState.Success(result.getOrNull())
                } else {
                    _spotDetailState.value = ParkingDetailState.Error(result.exceptionOrNull()?.message ?: "获取详情失败")
                }
            } catch (e: Exception) {
                _spotDetailState.value = ParkingDetailState.Error(e.message ?: "获取详情异常")
            }
        }
    }

    /**
     * 获取我的车位
     */
    fun getMyParkingSpots(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _mySpotsState.value = ParkingSearchState.Loading
            try {
                val result = parkingRepository.getMyParkingSpots(page, limit)
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    _mySpotsState.value = ParkingSearchState.Success(response?.spots ?: emptyList())
                } else {
                    _mySpotsState.value = ParkingSearchState.Error(result.exceptionOrNull()?.message ?: "获取我的车位失败")
                }
            } catch (e: Exception) {
                _mySpotsState.value = ParkingSearchState.Error(e.message ?: "获取我的车位异常")
            }
        }
    }

    /**
     * 创建停车位
     */
    fun createParkingSpot(request: CreateParkingSpotRequest) {
        viewModelScope.launch {
            _createSpotState.value = CreateSpotState.Loading
            try {
                val result = parkingRepository.createParkingSpot(request)
                if (result.isSuccess) {
                    _createSpotState.value = CreateSpotState.Success(result.getOrNull())
                } else {
                    _createSpotState.value = CreateSpotState.Error(result.exceptionOrNull()?.message ?: "创建失败")
                }
            } catch (e: Exception) {
                _createSpotState.value = CreateSpotState.Error(e.message ?: "创建异常")
            }
        }
    }

    /**
     * 更新停车位
     */
    fun updateParkingSpot(id: Int, request: CreateParkingSpotRequest) {
        viewModelScope.launch {
            _updateSpotState.value = CreateSpotState.Loading
            try {
                val result = parkingRepository.updateParkingSpot(id, request)
                if (result.isSuccess) {
                    _updateSpotState.value = CreateSpotState.Success(result.getOrNull())
                } else {
                    _updateSpotState.value = CreateSpotState.Error(result.exceptionOrNull()?.message ?: "更新失败")
                }
            } catch (e: Exception) {
                _updateSpotState.value = CreateSpotState.Error(e.message ?: "更新异常")
            }
        }
    }

    /**
     * 删除停车位
     */
    fun deleteParkingSpot(id: Int) {
        viewModelScope.launch {
            _deleteSpotState.value = DeleteSpotState.Loading
            try {
                val result = parkingRepository.deleteParkingSpot(id)
                if (result.isSuccess) {
                    _deleteSpotState.value = DeleteSpotState.Success
                } else {
                    _deleteSpotState.value = DeleteSpotState.Error(result.exceptionOrNull()?.message ?: "删除失败")
                }
            } catch (e: Exception) {
                _deleteSpotState.value = DeleteSpotState.Error(e.message ?: "删除异常")
            }
        }
    }

    /**
     * 检查车位可用性
     */
    fun checkAvailability(spotId: Int, startTime: String, endTime: String) {
        viewModelScope.launch {
            _availabilityState.value = AvailabilityState.Loading
            try {
                val result = parkingRepository.checkAvailability(spotId, startTime, endTime)
                if (result.isSuccess) {
                    _availabilityState.value = AvailabilityState.Success(result.getOrNull())
                } else {
                    _availabilityState.value = AvailabilityState.Error(result.exceptionOrNull()?.message ?: "检查可用性失败")
                }
            } catch (e: Exception) {
                _availabilityState.value = AvailabilityState.Error(e.message ?: "检查可用性异常")
            }
        }
    }

    /**
     * 添加收藏
     */
    fun addFavorite(spotId: Int) {
        viewModelScope.launch {
            _favoriteState.value = FavoriteState.Loading
            try {
                val result = parkingRepository.addFavorite(spotId)
                if (result.isSuccess) {
                    _favoriteState.value = FavoriteState.Success(spotId, true)
                } else {
                    _favoriteState.value = FavoriteState.Error(result.exceptionOrNull()?.message ?: "添加收藏失败")
                }
            } catch (e: Exception) {
                _favoriteState.value = FavoriteState.Error(e.message ?: "添加收藏异常")
            }
        }
    }

    /**
     * 移除收藏
     */
    fun removeFavorite(spotId: Int) {
        viewModelScope.launch {
            _favoriteState.value = FavoriteState.Loading
            try {
                val result = parkingRepository.removeFavorite(spotId)
                if (result.isSuccess) {
                    _favoriteState.value = FavoriteState.Success(spotId, false)
                } else {
                    _favoriteState.value = FavoriteState.Error(result.exceptionOrNull()?.message ?: "移除收藏失败")
                }
            } catch (e: Exception) {
                _favoriteState.value = FavoriteState.Error(e.message ?: "移除收藏异常")
            }
        }
    }

    /**
     * 获取收藏列表
     */
    fun getFavorites(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _favoritesState.value = ParkingSearchState.Loading
            try {
                val result = parkingRepository.getFavorites(page, limit)
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    _favoritesState.value = ParkingSearchState.Success(response?.spots ?: emptyList())
                } else {
                    _favoritesState.value = ParkingSearchState.Error(result.exceptionOrNull()?.message ?: "获取收藏列表失败")
                }
            } catch (e: Exception) {
                _favoritesState.value = ParkingSearchState.Error(e.message ?: "获取收藏列表异常")
            }
        }
    }

    /**
     * 重置搜索状态
     */
    fun resetSearchState() {
        _searchState.value = ParkingSearchState.Idle
    }

    /**
     * 重置车位详情状态
     */
    fun resetSpotDetailState() {
        _spotDetailState.value = ParkingDetailState.Idle
    }

    /**
     * 重置我的车位状态
     */
    fun resetMySpotsState() {
        _mySpotsState.value = ParkingSearchState.Idle
    }

    /**
     * 重置收藏状态
     */
    fun resetFavoriteState() {
        _favoriteState.value = FavoriteState.Idle
    }

    /**
     * 重置收藏列表状态
     */
    fun resetFavoritesState() {
        _favoritesState.value = ParkingSearchState.Idle
    }

    /**
     * 重置创建车位状态
     */
    fun resetCreateSpotState() {
        _createSpotState.value = CreateSpotState.Idle
    }

    /**
     * 重置更新车位状态
     */
    fun resetUpdateSpotState() {
        _updateSpotState.value = CreateSpotState.Idle
    }

    /**
     * 重置删除车位状态
     */
    fun resetDeleteSpotState() {
        _deleteSpotState.value = DeleteSpotState.Idle
    }

    /**
     * 重置可用性检查状态
     */
    fun resetAvailabilityState() {
        _availabilityState.value = AvailabilityState.Idle
    }
}

// ===== 状态密封类 =====

/**
 * 停车位搜索状态
 */
sealed class ParkingSearchState {
    object Idle : ParkingSearchState()
    object Loading : ParkingSearchState()
    data class Success(val spots: List<ParkingSpot>) : ParkingSearchState()
    data class Error(val message: String) : ParkingSearchState()
}

/**
 * 停车位详情状态
 */
sealed class ParkingDetailState {
    object Idle : ParkingDetailState()
    object Loading : ParkingDetailState()
    data class Success(val spot: ParkingSpot) : ParkingDetailState()
    data class Error(val message: String) : ParkingDetailState()
}

/**
 * 创建/更新停车位状态
 */
sealed class CreateSpotState {
    object Idle : CreateSpotState()
    object Loading : CreateSpotState()
    data class Success(val spot: ParkingSpot) : CreateSpotState()
    data class Error(val message: String) : CreateSpotState()
}

/**
 * 删除停车位状态
 */
sealed class DeleteSpotState {
    object Idle : DeleteSpotState()
    object Loading : DeleteSpotState()
    object Success : DeleteSpotState()
    data class Error(val message: String) : DeleteSpotState()
}

/**
 * 收藏状态
 */
sealed class FavoriteState {
    object Idle : FavoriteState()
    object Loading : FavoriteState()
    data class Success(val spotId: Int, val isFavorite: Boolean) : FavoriteState()
    data class Error(val message: String) : FavoriteState()
}

/**
 * 可用性检查状态
 */
sealed class AvailabilityState {
    object Idle : AvailabilityState()
    object Loading : AvailabilityState()
    data class Success(val isAvailable: Boolean) : AvailabilityState()
    data class Error(val message: String) : AvailabilityState()
}