package com.sharedparking.android.model

/**
 * 演示模式 - 不依赖服务端，使用本地假数据展示所有UI
 */
object DemoMode {
    var isEnabled = false

    val demoUser = User(
        id = 1,
        username = "演示用户",
        email = "demo@sharedparking.com",
        phone = "13800138000",
        realName = "张三",
        role = "user",
        isVerified = true,
        isActive = true
    )

    val demoSpots = listOf(
        ParkingSpot(
            id = 1, ownerId = 2, title = "国贸CBD地下停车场",
            description = "24小时监控，宽敞车位，靠近电梯入口",
            address = "北京市朝阳区建国路88号",
            latitude = 39.9087, longitude = 116.4605,
            pricePerHour = 8.0, pricePerDay = 60.0, priceUnit = "hour",
            availableSpots = 3, totalSpots = 5,
            isCovered = true, hasLighting = true, hasSecurity = true,
            hasCharging = true, hasCctv = true, is24hAccess = true,
            isActive = true, isApproved = true,
            viewCount = 256, bookCount = 48,
            ownerUsername = "王先生", avgRating = 4.8, reviewCount = 32,
            isFavorite = true
        ),
        ParkingSpot(
            id = 2, ownerId = 3, title = "中关村科技园车位",
            description = "近地铁口，周边餐饮配套齐全",
            address = "北京市海淀区中关村大街1号",
            latitude = 39.9814, longitude = 116.3103,
            pricePerHour = 6.0, pricePerDay = 45.0, priceUnit = "hour",
            availableSpots = 2, totalSpots = 3,
            isCovered = false, hasLighting = true, hasSecurity = true,
            hasCharging = false, hasCctv = true, is24hAccess = false,
            isActive = true, isApproved = true,
            viewCount = 189, bookCount = 36,
            ownerUsername = "李女士", avgRating = 4.6, reviewCount = 24
        ),
        ParkingSpot(
            id = 3, ownerId = 4, title = "望京SOHO地下车库",
            description = "临近写字楼，适合上班族长期停放",
            address = "北京市朝阳区望京街道望京东园四区",
            latitude = 39.9947, longitude = 116.4803,
            pricePerHour = 10.0, pricePerDay = 70.0, priceUnit = "hour",
            availableSpots = 5, totalSpots = 8,
            isCovered = true, hasLighting = true, hasSecurity = true,
            hasCharging = true, hasCctv = true, is24hAccess = true,
            isActive = true, isApproved = true,
            viewCount = 312, bookCount = 67,
            ownerUsername = "赵先生", avgRating = 4.9, reviewCount = 45,
            isFavorite = false
        ),
        ParkingSpot(
            id = 4, ownerId = 5, title = "西单商业区车位",
            description = "购物中心旁，周末停车首选",
            address = "北京市西城区西单北大街120号",
            latitude = 39.9138, longitude = 116.3749,
            pricePerHour = 12.0, pricePerDay = 80.0, priceUnit = "hour",
            availableSpots = 1, totalSpots = 2,
            isCovered = false, hasLighting = true, hasSecurity = false,
            hasCharging = false, hasCctv = true, is24hAccess = false,
            isActive = true, isApproved = true,
            viewCount = 145, bookCount = 22,
            ownerUsername = "刘先生", avgRating = 4.3, reviewCount = 18
        ),
        ParkingSpot(
            id = 5, ownerId = 6, title = "三里屯太古里车位",
            description = "核心商圈，夜间停车优惠",
            address = "北京市朝阳区三里屯路19号",
            latitude = 39.9336, longitude = 116.4540,
            pricePerHour = 15.0, pricePerDay = 100.0, priceUnit = "hour",
            availableSpots = 2, totalSpots = 4,
            isCovered = true, hasLighting = true, hasSecurity = true,
            hasCharging = false, hasCctv = true, is24hAccess = true,
            isActive = true, isApproved = true,
            viewCount = 423, bookCount = 89,
            ownerUsername = "陈女士", avgRating = 4.7, reviewCount = 56
        )
    )

    val demoBookings = listOf(
        Booking(
            id = 1, userId = 1, spotId = 1,
            vehiclePlateNumber = "京A12345", vehicleBrand = "Tesla", vehicleModel = "Model 3",
            startTime = "2026-04-26 09:00:00", endTime = "2026-04-26 17:00:00",
            durationHours = 8.0, totalPrice = 64.0,
            status = "confirmed",
            checkInCode = "ABC123",
            spotTitle = "国贸CBD地下停车场", spotAddress = "北京市朝阳区建国路88号",
            pricePerHour = 8.0, ownerUsername = "王先生"
        ),
        Booking(
            id = 2, userId = 1, spotId = 3,
            vehiclePlateNumber = "京B67890", vehicleBrand = "BYD", vehicleModel = "汉EV",
            startTime = "2026-04-27 08:00:00", endTime = "2026-04-27 18:00:00",
            durationHours = 10.0, totalPrice = 100.0,
            status = "pending",
            spotTitle = "望京SOHO地下车库", spotAddress = "北京市朝阳区望京街道望京东园四区",
            pricePerHour = 10.0, ownerUsername = "赵先生"
        ),
        Booking(
            id = 3, userId = 1, spotId = 5,
            vehiclePlateNumber = "京C13579", vehicleBrand = "NIO", vehicleModel = "ET5",
            startTime = "2026-04-25 19:00:00", endTime = "2026-04-25 22:00:00",
            durationHours = 3.0, totalPrice = 45.0,
            status = "completed",
            spotTitle = "三里屯太古里车位", spotAddress = "北京市朝阳区三里屯路19号",
            pricePerHour = 15.0, ownerUsername = "陈女士"
        ),
        Booking(
            id = 4, userId = 1, spotId = 2,
            vehiclePlateNumber = "京A12345", vehicleBrand = "Tesla", vehicleModel = "Model 3",
            startTime = "2026-04-24 09:00:00", endTime = "2026-04-24 12:00:00",
            durationHours = 3.0, totalPrice = 18.0,
            status = "cancelled",
            cancelledBy = "user", cancellationReason = "行程变更",
            spotTitle = "中关村科技园车位", spotAddress = "北京市海淀区中关村大街1号",
            pricePerHour = 6.0, ownerUsername = "李女士"
        ),
        Booking(
            id = 5, userId = 1, spotId = 4,
            vehiclePlateNumber = "京B67890", vehicleBrand = "BYD", vehicleModel = "汉EV",
            startTime = "2026-04-23 14:00:00", endTime = "2026-04-23 16:00:00",
            durationHours = 2.0, totalPrice = 24.0,
            status = "completed",
            spotTitle = "西单商业区车位", spotAddress = "北京市西城区西单北大街120号",
            pricePerHour = 12.0, ownerUsername = "刘先生"
        )
    )

    val demoPayment = Payment(
        id = 1, bookingId = 2, userId = 1, amount = 100.0,
        paymentMethod = "alipay", status = "pending",
        createdAt = "2026-04-26 10:00:00"
    )

    val demoPagination = Pagination(page = 1, limit = 20, total = 5, pages = 1)

    fun reset() {
        isEnabled = false
    }
}
