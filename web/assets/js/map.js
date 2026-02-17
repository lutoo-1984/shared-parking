/**
 * 共享停车位平台 - 地图集成模块
 * 百度地图API集成
 * 版本: 1.0.0
 */

class ParkingMap {
    /**
     * 构造函数
     * @param {string} containerId - 地图容器ID
     */
    constructor(containerId) {
        this.containerId = containerId;
        this.map = null;
        this.markers = [];
        this.infoWindows = [];
        this.currentLocation = null;
        this.currentMarker = null;
        this.parkingSpots = [];
    }

    /**
     * 初始化地图
     * @param {number} lat - 纬度
     * @param {number} lng - 经度
     * @param {number} zoom - 缩放级别
     */
    initMap(lat = 39.909186, lng = 116.397389, zoom = 13) {
        if (typeof BMap === 'undefined') {
            console.error('百度地图API未加载');
            this.showMapError();
            return;
        }

        try {
            const point = new BMap.Point(lng, lat);
            this.map = new BMap.Map(this.containerId);
            this.map.centerAndZoom(point, zoom);
            this.map.enableScrollWheelZoom(true);

            // 添加地图控件
            this.addMapControls();

            // 尝试定位用户位置
            this.locateUser();

            console.log('百度地图初始化成功');
        } catch (error) {
            console.error('地图初始化失败:', error);
            this.showMapError();
        }
    }

    /**
     * 显示地图错误
     */
    showMapError() {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        container.innerHTML = `
            <div style="
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                height: 100%;
                color: #666;
                text-align: center;
                padding: 20px;
            ">
                <i class="fas fa-map-marked-alt" style="font-size: 48px; color: #ccc; margin-bottom: 20px;"></i>
                <h3 style="margin-bottom: 10px;">地图加载失败</h3>
                <p style="margin-bottom: 20px;">无法加载地图服务，请检查网络连接或刷新页面</p>
                <button onclick="window.location.reload()" style="
                    padding: 10px 20px;
                    background-color: #3498db;
                    color: white;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                ">
                    <i class="fas fa-redo"></i> 刷新页面
                </button>
            </div>
        `;
    }

    /**
     * 添加地图控件
     */
    addMapControls() {
        if (!this.map) return;

        // 缩放控件
        this.map.addControl(new BMap.NavigationControl({
            anchor: BMAP_ANCHOR_TOP_LEFT,
            type: BMAP_NAVIGATION_CONTROL_LARGE
        }));

        // 比例尺
        this.map.addControl(new BMap.ScaleControl({
            anchor: BMAP_ANCHOR_BOTTOM_LEFT
        }));

        // 地图类型控件
        this.map.addControl(new BMap.MapTypeControl({
            anchor: BMAP_ANCHOR_TOP_RIGHT,
            mapTypes: [BMAP_NORMAL_MAP, BMAP_SATELLITE_MAP, BMAP_HYBRID_MAP]
        }));

        // 版权控件
        this.map.addControl(new BMap.CopyrightControl({
            anchor: BMAP_ANCHOR_BOTTOM_RIGHT
        }));
    }

    /**
     * 定位用户位置
     */
    locateUser() {
        if (!this.map) return;

        const geolocation = new BMap.Geolocation();

        geolocation.getCurrentPosition(
            (position) => {
                if (geolocation.getStatus() === BMAP_STATUS_SUCCESS) {
                    const point = new BMap.Point(
                        position.point.lng,
                        position.point.lat
                    );

                    this.currentLocation = {
                        lat: position.point.lat,
                        lng: position.point.lng
                    };

                    // 添加用户位置标记
                    this.addUserMarker(point);

                    // 将地图中心移动到用户位置
                    this.map.centerAndZoom(point, 15);

                    // 搜索附近的停车位
                    this.searchNearbyParkingSpots();
                } else {
                    console.warn('获取位置失败:', geolocation.getStatus());
                    this.showLocationError();
                }
            },
            { enableHighAccuracy: true }
        );
    }

    /**
     * 添加用户位置标记
     */
    addUserMarker(point) {
        if (!this.map) return;

        // 移除旧的用户标记
        if (this.currentMarker) {
            this.map.removeOverlay(this.currentMarker);
        }

        // 创建用户位置标记
        const icon = new BMap.Icon(
            'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="%233498db"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="4" fill="white"/></svg>',
            new BMap.Size(24, 24)
        );

        this.currentMarker = new BMap.Marker(point, { icon: icon });
        this.map.addOverlay(this.currentMarker);

        // 添加信息窗口
        const infoWindow = new BMap.InfoWindow(`
            <div style="padding: 10px; max-width: 200px;">
                <h4 style="margin: 0 0 5px 0; color: #3498db;">
                    <i class="fas fa-user-circle"></i> 我的位置
                </h4>
                <p style="margin: 0; color: #666; font-size: 12px;">
                    纬度: ${point.lat.toFixed(6)}<br>
                    经度: ${point.lng.toFixed(6)}
                </p>
            </div>
        `);

        this.currentMarker.addEventListener('click', () => {
            this.map.openInfoWindow(infoWindow, point);
        });
    }

    /**
     * 显示定位错误
     */
    showLocationError() {
        // 显示定位失败提示
        const infoWindow = new BMap.InfoWindow(`
            <div style="padding: 15px; max-width: 250px;">
                <h4 style="margin: 0 0 10px 0; color: #e74c3c;">
                    <i class="fas fa-exclamation-triangle"></i> 定位失败
                </h4>
                <p style="margin: 0 0 10px 0; color: #666;">
                    无法获取您的位置，请检查浏览器定位权限或手动选择位置。
                </p>
                <button onclick="map.searchNearbyParkingSpots()" style="
                    padding: 5px 10px;
                    background-color: #3498db;
                    color: white;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                    font-size: 12px;
                ">
                    <i class="fas fa-search"></i> 搜索附近车位
                </button>
            </div>
        `);

        this.map.openInfoWindow(infoWindow, this.map.getCenter());
    }

    /**
     * 搜索附近的停车位
     */
    async searchNearbyParkingSpots(lat = null, lng = null, radius = 5000) {
        if (!this.map) return;

        try {
            // 显示加载状态
            this.showLoading(true);

            const searchLat = lat || (this.currentLocation ? this.currentLocation.lat : 39.909186);
            const searchLng = lng || (this.currentLocation ? this.currentLocation.lng : 116.397389);

            // 调用API搜索停车位
            const response = await fetch(
                `/api/parking/search?lat=${searchLat}&lng=${searchLng}&radius=${radius}`
            );

            if (!response.ok) {
                throw new Error(`搜索失败: ${response.status}`);
            }

            const data = await response.json();

            if (data.success) {
                this.parkingSpots = data.data || [];
                this.displayParkingSpots(this.parkingSpots);
            } else {
                throw new Error(data.message || '搜索失败');
            }
        } catch (error) {
            console.error('搜索停车位失败:', error);
            this.showError('搜索失败，请稍后重试');
        } finally {
            this.showLoading(false);
        }
    }

    /**
     * 显示停车位标记
     */
    displayParkingSpots(spots) {
        if (!this.map) return;

        // 清除旧的标记
        this.clearMarkers();

        // 添加新的标记
        spots.forEach(spot => {
            this.addParkingSpotMarker(spot);
        });

        // 如果只有一个标记，自动打开信息窗口
        if (spots.length === 1) {
            setTimeout(() => {
                if (this.infoWindows[0]) {
                    this.map.openInfoWindow(this.infoWindows[0], this.markers[0].getPosition());
                }
            }, 500);
        }

        console.log(`显示 ${spots.length} 个停车位`);
    }

    /**
     * 添加停车位标记
     */
    addParkingSpotMarker(spot) {
        if (!this.map) return;

        const point = new BMap.Point(spot.longitude, spot.latitude);

        // 创建自定义图标
        const icon = new BMap.Icon(
            this.createParkingIcon(spot),
            new BMap.Size(32, 32)
        );

        const marker = new BMap.Marker(point, { icon: icon });
        this.map.addOverlay(marker);
        this.markers.push(marker);

        // 创建信息窗口
        const infoWindow = this.createInfoWindow(spot);
        this.infoWindows.push(infoWindow);

        // 点击标记打开信息窗口
        marker.addEventListener('click', () => {
            this.closeAllInfoWindows();
            this.map.openInfoWindow(infoWindow, point);
        });

        // 鼠标悬停效果
        marker.addEventListener('mouseover', () => {
            marker.setIcon(new BMap.Icon(
                this.createParkingIcon(spot, true),
                new BMap.Size(36, 36)
            ));
        });

        marker.addEventListener('mouseout', () => {
            marker.setIcon(new BMap.Icon(
                this.createParkingIcon(spot),
                new BMap.Size(32, 32)
            ));
        });
    }

    /**
     * 创建停车位图标
     */
    createParkingIcon(spot, hover = false) {
        const color = spot.is_available ? '#2ecc71' : '#e74c3c';
        const size = hover ? 36 : 32;
        const price = spot.price_per_hour || 0;

        return `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="10" fill="${color}" fill-opacity="${hover ? '0.9' : '0.8'}"/>
            <text x="12" y="16" text-anchor="middle" fill="white" font-size="10" font-weight="bold">
                ¥${price}
            </text>
        </svg>`;
    }

    /**
     * 创建信息窗口
     */
    createInfoWindow(spot) {
        const content = `
            <div style="padding: 15px; max-width: 300px;">
                <h4 style="margin: 0 0 10px 0; color: #333;">
                    <i class="fas fa-parking"></i> ${spot.title || '停车位'}
                </h4>

                <div style="margin-bottom: 10px;">
                    <p style="margin: 0 0 5px 0; color: #666; font-size: 13px;">
                        <i class="fas fa-map-marker-alt"></i> ${spot.address || '未知地址'}
                    </p>
                    <p style="margin: 0 0 5px 0; color: #666; font-size: 13px;">
                        <i class="fas fa-clock"></i> ${spot.price_per_hour}元/小时
                        ${spot.price_per_day ? `(${spot.price_per_day}元/天)` : ''}
                    </p>
                </div>

                <div style="display: flex; flex-wrap: wrap; gap: 5px; margin-bottom: 15px;">
                    ${spot.is_available ?
                        '<span style="background-color: #2ecc71; color: white; padding: 2px 8px; border-radius: 10px; font-size: 12px;">可用</span>' :
                        '<span style="background-color: #e74c3c; color: white; padding: 2px 8px; border-radius: 10px; font-size: 12px;">已占用</span>'
                    }
                    ${spot.amenities && spot.amenities.includes('security') ?
                        '<span style="background-color: #3498db; color: white; padding: 2px 8px; border-radius: 10px; font-size: 12px;">安保</span>' : ''
                    }
                    ${spot.amenities && spot.amenities.includes('covered') ?
                        '<span style="background-color: #9b59b6; color: white; padding: 2px 8px; border-radius: 10px; font-size: 12px;">有顶棚</span>' : ''
                    }
                </div>

                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div style="color: #f39c12; font-size: 14px;">
                        ${'★'.repeat(Math.floor(spot.avg_rating || 0))}${'☆'.repeat(5 - Math.floor(spot.avg_rating || 0))}
                        <span style="color: #666; font-size: 12px; margin-left: 5px;">
                            ${spot.avg_rating ? spot.avg_rating.toFixed(1) : '0.0'}
                        </span>
                    </div>

                    <a href="/parking/${spot.id}" style="
                        display: inline-block;
                        padding: 5px 15px;
                        background-color: #3498db;
                        color: white;
                        text-decoration: none;
                        border-radius: 4px;
                        font-size: 12px;
                        transition: background-color 0.3s;
                    " onmouseover="this.style.backgroundColor='#2980b9'" onmouseout="this.style.backgroundColor='#3498db'">
                        <i class="fas fa-info-circle"></i> 查看详情
                    </a>
                </div>
            </div>
        `;

        return new BMap.InfoWindow(content);
    }

    /**
     * 清除所有标记
     */
    clearMarkers() {
        if (!this.map) return;

        this.markers.forEach(marker => {
            this.map.removeOverlay(marker);
        });

        this.markers = [];
        this.infoWindows = [];
    }

    /**
     * 关闭所有信息窗口
     */
    closeAllInfoWindows() {
        this.infoWindows.forEach(window => {
            this.map.closeInfoWindow();
        });
    }

    /**
     * 显示加载状态
     */
    showLoading(show) {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        let loading = container.querySelector('.map-loading');

        if (show) {
            if (!loading) {
                loading = document.createElement('div');
                loading.className = 'map-loading';
                loading.innerHTML = `
                    <div style="
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        background-color: rgba(255, 255, 255, 0.8);
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        z-index: 1000;
                    ">
                        <div style="text-align: center;">
                            <i class="fas fa-spinner fa-spin" style="font-size: 30px; color: #3498db; margin-bottom: 10px;"></i>
                            <p style="color: #666; margin: 0;">搜索中...</p>
                        </div>
                    </div>
                `;
                container.style.position = 'relative';
                container.appendChild(loading);
            }
        } else {
            if (loading) {
                loading.remove();
            }
        }
    }

    /**
     * 显示错误信息
     */
    showError(message) {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        const errorDiv = document.createElement('div');
        errorDiv.className = 'map-error';
        errorDiv.innerHTML = `
            <div style="
                position: absolute;
                top: 10px;
                left: 10px;
                right: 10px;
                background-color: rgba(231, 76, 60, 0.9);
                color: white;
                padding: 10px 15px;
                border-radius: 4px;
                z-index: 1000;
                display: flex;
                align-items: center;
                justify-content: space-between;
                font-size: 14px;
            ">
                <span><i class="fas fa-exclamation-circle"></i> ${message}</span>
                <button onclick="this.parentElement.remove()" style="
                    background: none;
                    border: none;
                    color: white;
                    cursor: pointer;
                    padding: 0;
                    margin-left: 10px;
                ">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;

        container.style.position = 'relative';
        container.appendChild(errorDiv);

        // 5秒后自动移除错误信息
        setTimeout(() => {
            if (errorDiv.parentElement) {
                errorDiv.remove();
            }
        }, 5000);
    }

    /**
     * 添加搜索控件
     */
    addSearchControl() {
        if (!this.map) return;

        const searchControl = new BMapLib.SearchControl({
            anchor: BMAP_ANCHOR_TOP_RIGHT,
            panel: false,
            autoViewport: true,
            selectFirstResult: false
        });

        this.map.addControl(searchControl);

        // 监听搜索完成事件
        searchControl.addEventListener('onconfirm', (e) => {
            const results = e.results;
            if (results && results.length > 0) {
                const point = results[0].point;
                this.map.centerAndZoom(point, 15);
                this.searchNearbyParkingSpots(point.lat, point.lng);
            }
        });
    }

    /**
     * 绘制搜索范围
     */
    drawSearchCircle(center, radius) {
        if (!this.map) return;

        // 移除旧的圆形
        if (this.searchCircle) {
            this.map.removeOverlay(this.searchCircle);
        }

        // 创建圆形
        const circle = new BMap.Circle(center, radius, {
            strokeColor: '#3498db',
            strokeWeight: 2,
            strokeOpacity: 0.5,
            fillColor: '#3498db',
            fillOpacity: 0.1
        });

        this.map.addOverlay(circle);
        this.searchCircle = circle;
    }

    /**
     * 添加点击事件获取坐标
     */
    enableCoordinatePicker(callback) {
        if (!this.map) return;

        this.map.addEventListener('click', (e) => {
            const point = e.point;

            if (callback && typeof callback === 'function') {
                callback(point.lat, point.lng);
            }

            // 显示坐标信息
            const infoWindow = new BMap.InfoWindow(`
                <div style="padding: 10px; max-width: 200px;">
                    <h4 style="margin: 0 0 5px 0; color: #3498db;">
                        <i class="fas fa-crosshairs"></i> 选择的位置
                    </h4>
                    <p style="margin: 0 0 5px 0; color: #666; font-size: 12px;">
                        纬度: ${point.lat.toFixed(6)}<br>
                        经度: ${point.lng.toFixed(6)}
                    </p>
                    <button onclick="map.searchNearbyParkingSpots(${point.lat}, ${point.lng})" style="
                        padding: 3px 8px;
                        background-color: #3498db;
                        color: white;
                        border: none;
                        border-radius: 3px;
                        cursor: pointer;
                        font-size: 11px;
                        margin-top: 5px;
                    ">
                        搜索此位置
                    </button>
                </div>
            `);

            this.map.openInfoWindow(infoWindow, point);
        });
    }

    /**
     * 计算路线
     */
    calculateRoute(start, end, mode = BMAP_TRANSIT_MODE) {
        if (!this.map) return;

        const routeControl = new BMapLib.DrivingRoute(this.map, {
            renderOptions: {
                map: this.map,
                autoViewport: true,
                panel: 'route-panel'
            },
            policy: mode
        });

        routeControl.search(new BMap.Point(start.lng, start.lat), new BMap.Point(end.lng, end.lat));
    }

    /**
     * 导出为图片
     */
    exportToImage() {
        if (!this.map) return;

        const canvas = document.createElement('canvas');
        const container = document.getElementById(this.containerId);
        const ctx = canvas.getContext('2d');

        canvas.width = container.offsetWidth;
        canvas.height = container.offsetHeight;

        // 这里需要实际的地图截图逻辑
        // 注意：百度地图API没有直接的截图方法
        // 实际项目中可能需要使用服务器端截图服务

        alert('地图截图功能需要服务器端支持');
    }

    /**
     * 销毁地图实例
     */
    destroy() {
        if (this.map) {
            this.clearMarkers();
            this.map = null;
        }
    }
}

// 全局地图实例
let map = null;

// 初始化地图
function initParkingMap(containerId = 'map-container', lat = 39.909186, lng = 116.397389) {
    map = new ParkingMap(containerId);
    map.initMap(lat, lng);
    return map;
}

// 全局导出
if (typeof window !== 'undefined') {
    window.ParkingMap = ParkingMap;
    window.initParkingMap = initParkingMap;
    window.map = map;
}

// 自动初始化页面上的地图
document.addEventListener('DOMContentLoaded', function() {
    const mapContainers = document.querySelectorAll('[data-auto-init-map]');

    mapContainers.forEach(container => {
        const containerId = container.id;
        const lat = parseFloat(container.dataset.lat) || 39.909186;
        const lng = parseFloat(container.dataset.lng) || 116.397389;
        const zoom = parseInt(container.dataset.zoom) || 13;

        if (containerId) {
            setTimeout(() => {
                const parkingMap = new ParkingMap(containerId);
                parkingMap.initMap(lat, lng, zoom);

                // 如果有搜索参数，执行搜索
                const urlParams = new URLSearchParams(window.location.search);
                const searchLat = urlParams.get('lat');
                const searchLng = urlParams.get('lng');
                const radius = urlParams.get('radius') || 5000;

                if (searchLat && searchLng) {
                    parkingMap.searchNearbyParkingSpots(
                        parseFloat(searchLat),
                        parseFloat(searchLng),
                        parseInt(radius)
                    );
                }
            }, 100);
        }
    });
});