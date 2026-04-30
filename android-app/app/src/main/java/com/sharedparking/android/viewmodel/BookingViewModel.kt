package com.sharedparking.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sharedparking.android.model.*
import com.sharedparking.android.repository.BookingRepository
import com.sharedparking.android.utils.Resource
import kotlinx.coroutines.launch
import java.util.Date

/**
 * 预订相关的ViewModel
 */
class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val bookingRepository = BookingRepository(application)

    // 表单状态
    private val _formState = MutableLiveData<BookingFormState>()
    val formState: LiveData<BookingFormState> = _formState

    // 创建预订状态
    private val _createBookingState = MutableLiveData<Resource<Booking>>()
    val createBookingState: LiveData<Resource<Booking>> = _createBookingState

    // 预订详情状态
    private val _bookingDetailState = MutableLiveData<Resource<Booking>>()
    val bookingDetailState: LiveData<Resource<Booking>> = _bookingDetailState

    // 取消预订状态
    private val _cancelBookingState = MutableLiveData<Resource<Unit>>()
    val cancelBookingState: LiveData<Resource<Unit>> = _cancelBookingState

    // 我的预订列表状态
    private val _myBookingsState = MutableLiveData<Resource<List<Booking>>>()
    val myBookingsState: LiveData<Resource<List<Booking>>> = _myBookingsState

    // 可用性检查状态
    private val _availabilityState = MutableLiveData<Resource<Boolean>>()
    val availabilityState: LiveData<Resource<Boolean>> = _availabilityState

    // 停车位信息
    private val _spot = MutableLiveData<ParkingSpot?>()
    val spot: LiveData<ParkingSpot?> = _spot

    // 时间信息
    private val _startTime = MutableLiveData<Date?>()
    val startTime: LiveData<Date?> = _startTime

    private val _endTime = MutableLiveData<Date?>()
    val endTime: LiveData<Date?> = _endTime

    // 车辆信息
    private val _vehicleInfo = MutableLiveData<VehicleInfo>(VehicleInfo())
    val vehicleInfo: LiveData<VehicleInfo> = _vehicleInfo

    // 价格信息
    private val _durationHours = MutableLiveData<Double>(0.0)
    val durationHours: LiveData<Double> = _durationHours

    private val _totalPrice = MutableLiveData<Double>(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

    // ===== 公开方法 =====

    /**
     * 设置停车位信息
     */
    fun setSpot(spot: ParkingSpot) {
        _spot.value = spot
        calculatePrice()
    }

    /**
     * 设置开始时间
     */
    fun setStartTime(date: Date) {
        _startTime.value = date
        calculatePrice()
        checkAvailability()
    }

    /**
     * 设置结束时间
     */
    fun setEndTime(date: Date) {
        _endTime.value = date
        calculatePrice()
        checkAvailability()
    }

    /**
     * 更新车辆信息
     */
    fun updateVehicleInfo(
        plateNumber: String,
        brand: String,
        model: String,
        color: String = ""
    ) {
        _vehicleInfo.value = VehicleInfo(
            plateNumber = plateNumber,
            brand = brand,
            model = model,
            color = color
        )
        validateForm()
    }

    /**
     * 验证表单
     */
    fun validateForm(): Boolean {
        val spot = _spot.value
        val startTime = _startTime.value
        val endTime = _endTime.value
        val vehicleInfo = _vehicleInfo.value ?: VehicleInfo()

        var isValid = true

        // 检查停车位
        if (spot == null) {
            // 这里不显示错误，因为停车位是从详情页传入的
            isValid = false
        }

        // 检查开始时间
        if (startTime == null) {
            // 错误在UI层处理
            isValid = false
        }

        // 检查结束时间
        if (endTime == null) {
            // 错误在UI层处理
            isValid = false
        }

        // 检查时间范围
        if (startTime != null && endTime != null) {
            val (timeValid, timeMessage) = bookingRepository.validateTimeRange(startTime, endTime)
            if (!timeValid) {
                // 错误在UI层处理
                isValid = false
            }
        }

        // 检查车牌号
        if (vehicleInfo.plateNumber.isBlank()) {
            // 错误在UI层处理
            isValid = false
        } else if (!bookingRepository.validatePlateNumber(vehicleInfo.plateNumber)) {
            // 错误在UI层处理
            isValid = false
        }

        // 检查车辆品牌
        if (vehicleInfo.brand.isBlank()) {
            // 错误在UI层处理
            isValid = false
        }

        // 检查车辆型号
        if (vehicleInfo.model.isBlank()) {
            // 错误在UI层处理
            isValid = false
        }

        _formState.value = BookingFormState(isDataValid = isValid)
        return isValid
    }

    /**
     * 创建预订
     */
    fun createBooking(notes: String? = null) {
        val spot = _spot.value
        val startTime = _startTime.value
        val endTime = _endTime.value
        val vehicleInfo = _vehicleInfo.value

        if (spot == null || startTime == null || endTime == null || vehicleInfo == null) {
            _createBookingState.value = Resource.Error("请填写完整的预订信息")
            return
        }

        if (!validateForm()) {
            _createBookingState.value = Resource.Error("请检查表单信息")
            return
        }

        viewModelScope.launch {
            _createBookingState.value = Resource.Loading()
            try {
                val result = bookingRepository.createBooking(
                    spotId = spot.id,
                    vehiclePlateNumber = vehicleInfo.plateNumber,
                    vehicleBrand = vehicleInfo.brand,
                    vehicleModel = vehicleInfo.model,
                    vehicleColor = vehicleInfo.color,
                    startTime = startTime,
                    endTime = endTime,
                    notes = notes
                )

                _createBookingState.value = result
            } catch (e: Exception) {
                _createBookingState.value = Resource.Error(e.message ?: "创建预订失败")
            }
        }
    }

    /**
     * 检查可用性
     */
    private fun checkAvailability() {
        val spot = _spot.value
        val startTime = _startTime.value
        val endTime = _endTime.value

        if (spot == null || startTime == null || endTime == null) {
            return
        }

        viewModelScope.launch {
            _availabilityState.value = Resource.Loading()
            try {
                val result = bookingRepository.checkAvailability(spot.id, startTime, endTime)
                _availabilityState.value = result
            } catch (e: Exception) {
                _availabilityState.value = Resource.Error(e.message ?: "检查可用性失败")
            }
        }
    }

    /**
     * 计算价格
     */
    private fun calculatePrice() {
        val spot = _spot.value
        val startTime = _startTime.value
        val endTime = _endTime.value

        if (spot == null || startTime == null || endTime == null) {
            _durationHours.value = 0.0
            _totalPrice.value = 0.0
            return
        }

        val duration = bookingRepository.calculateDurationHours(startTime, endTime)
        val price = bookingRepository.calculatePrice(startTime, endTime, spot.pricePerHour)

        _durationHours.value = duration
        _totalPrice.value = price
    }

    /**
     * 获取预订详情
     */
    fun getBooking(bookingId: Int) {
        viewModelScope.launch {
            _bookingDetailState.value = Resource.Loading()
            try {
                val result = bookingRepository.getBooking(bookingId)
                _bookingDetailState.value = result
            } catch (e: Exception) {
                _bookingDetailState.value = Resource.Error(e.message ?: "获取预订详情失败")
            }
        }
    }

    /**
     * 获取我的预订列表
     */
    fun getMyBookings() {
        viewModelScope.launch {
            _myBookingsState.value = Resource.Loading()
            try {
                val result = bookingRepository.getMyBookings()
                if (result is Resource.Success) {
                    val response = result.data
                    _myBookingsState.value = Resource.Success(response?.bookings ?: emptyList())
                } else if (result is Resource.Error) {
                    _myBookingsState.value = Resource.Error(result.message ?: "加载失败")
                }
            } catch (e: Exception) {
                _myBookingsState.value = Resource.Error(e.message ?: "网络错误")
            }
        }
    }

    /**
     * 取消预订
     */
    fun cancelBooking(bookingId: Int) {
        viewModelScope.launch {
            _cancelBookingState.value = Resource.Loading()
            try {
                val result = bookingRepository.cancelBooking(bookingId)
                _cancelBookingState.value = when (result) {
                    is Resource.Success -> Resource.Success(Unit)
                    is Resource.Error -> result as Resource.Error<Unit>
                    else -> Resource.Error("取消失败")
                }
            } catch (e: Exception) {
                _cancelBookingState.value = Resource.Error(e.message ?: "取消失败")
            }
        }
    }

    /**
     * 重置创建状态
     */
    fun resetCreateState() {
        _createBookingState.value = Resource.Idle()
    }

    /**
     * 重置预订详情状态
     */
    fun resetBookingDetailState() {
        _bookingDetailState.value = Resource.Idle()
    }

    /**
     * 重置取消预订状态
     */
    fun resetCancelBookingState() {
        _cancelBookingState.value = Resource.Idle()
    }

    /**
     * 重置可用性状态
     */
    fun resetAvailabilityState() {
        _availabilityState.value = Resource.Idle()
    }

    /**
     * 获取当前价格信息
     */
    fun getPriceInfo(): PriceInfo {
        return PriceInfo(
            pricePerHour = _spot.value?.pricePerHour ?: 0.0,
            durationHours = _durationHours.value ?: 0.0,
            totalPrice = _totalPrice.value ?: 0.0
        )
    }

    /**
     * 价格信息数据类
     */
    data class PriceInfo(
        val pricePerHour: Double,
        val durationHours: Double,
        val totalPrice: Double
    )
}