/**
 * 共享停车位平台 - 地图集成模块
 * 高德地图API集成（AMap）
 * 版本: 1.1.0
 */

class ParkingMap {
    /**
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
        this.searchCircle = null;
    }

    /**
     * 初始化地图
     */
    initMap(lat = 39.909186, lng = 116.397389, zoom = 13) {
        if (typeof AMap === 'undefined') {
            console.error('高德地图API未加载');
            this.showMapError();
            return;
        }

        try {
            this.map = new AMap.Map(this.containerId, {
                zoom: zoom,
                center: [lng, lat],
                resizeEnable: true,
                mapStyle: 'amap://styles/light'
            });

            // 异步加载控件插件
            this.loadControls();

            // 尝试定位用户
            this.locateUser();

            console.log('高德地图初始化成功');
        } catch (error) {
            console.error('地图初始化失败:', error);
            this.showMapError();
        }
    }

    /**
     * 异步加载高德地图控件插件
     */
    loadControls() {
        if (!this.map) return;

        AMap.plugin(['AMap.ToolBar', 'AMap.Scale', 'AMap.MapType'], () => {
            this.map.addControl(new AMap.ToolBar({ position: 'LT' }));
            this.map.addControl(new AMap.Scale({ position: 'LB' }));
            this.map.addControl(new AMap.MapType({ position: 'RT', defaultType: 0 }));
            console.log('→ 地图控件加载完成');
        });
    }
        const container = document.getElementById(this.containerId);
        if (!container) return;
        container.innerHTML = `
            <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;height:100%;color:#666;text-align:center;padding:20px;">
                <i class="fas fa-map-marked-alt" style="font-size:48px;color:#ccc;margin-bottom:20px;"></i>
                <h3 style="margin-bottom:10px;">地图加载失败</h3>
                <p style="margin-bottom:20px;">无法加载地图服务，请检查网络连接或刷新页面</p>
                <button onclick="window.location.reload()" style="padding:10px 20px;background:#4A90D9;color:#fff;border:none;border-radius:4px;cursor:pointer;">
                    <i class="fas fa-redo"></i> 刷新页面
                </button>
            </div>
        `;
    }

    /**
     * 定位用户位置
     */
    locateUser() {
        if (!this.map) return;

        AMap.plugin('AMap.Geolocation', () => {
            const geolocation = new AMap.Geolocation({
                enableHighAccuracy: true,
                timeout: 10000,
                buttonPosition: 'RB',
                buttonOffset: new AMap.Pixel(10, 20),
                zoomToAccuracy: true
            });

            this.map.addControl(geolocation);

            geolocation.getCurrentPosition((status, result) => {
                if (status === 'complete') {
                    const pos = result.position;
                    this.currentLocation = {
                        lat: pos.getLat(),
                        lng: pos.getLng()
                    };

                    // 添加用户位置标记
                    this.addUserMarker(pos);

                    // 地图中心移到用户位置
                    this.map.setZoomAndCenter(15, pos);

                    // 搜索附近车位
                    this.searchNearbyParkingSpots();
                } else {
                    console.warn('定位失败:', result.message || '未知错误');
                    this.showLocationError();
                }
            });
        });
    }

    /**
     * 添加用户位置标记
     */
    addUserMarker(position) {
        if (!this.map) return;

        // 移除旧标记
        if (this.currentMarker) {
            this.map.remove(this.currentMarker);
        }

        // 创建用户位置标记
        this.currentMarker = new AMap.Marker({
            position: position,
            map: this.map,
            icon: new AMap.Icon({
                size: new AMap.Size(24, 24),
                image: 'data:image/svg+xml;utf8,' + encodeURIComponent(
                    '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">' +
                    '<circle cx="12" cy="12" r="10" fill="#4A90D9"/><circle cx="12" cy="12" r="4" fill="white"/></svg>'
                ),
                imageSize: new AMap.Size(24, 24)
            })
        });

        // 信息窗口
        const infoWindow = new AMap.InfoWindow({
            content: `
                <div style="padding:10px;max-width:200px;">
                    <h4 style="margin:0 0 5px;color:#4A90D9;"><i class="fas fa-user-circle"></i> 我的位置</h4>
                    <p style="margin:0;color:#666;font-size:12px;">
                        纬度: ${position.getLat().toFixed(6)}<br>
                        经度: ${position.getLng().toFixed(6)}
                    </p>
                </div>
            `
        });

        this.currentMarker.on('click', () => {
            infoWindow.open(this.map, position);
        });
    }

    /**
     * 定位失败提示
     */
    showLocationError() {
        const center = this.map.getCenter();
        const infoWindow = new AMap.InfoWindow({
            content: `
                <div style="padding:15px;max-width:250px;">
                    <h4 style="margin:0 0 10px;color:#e74c3c;"><i class="fas fa-exclamation-triangle"></i> 定位失败</h4>
                    <p style="margin:0 0 10px;color:#666;">无法获取您的位置，请检查权限或手动选择位置。</p>
                </div>
            `
        });
        infoWindow.open(this.map, center);
    }

    /**
     * 搜索附近停车位
     */
    async searchNearbyParkingSpots(lat = null, lng = null, radius = 5000) {
        if (!this.map) return;

        try {
            this.showLoading(true);

            const searchLat = lat || (this.currentLocation ? this.currentLocation.lat : 39.909186);
            const searchLng = lng || (this.currentLocation ? this.currentLocation.lng : 116.397389);

            const response = await fetch(
                `/api/parking/search?lat=${searchLat}&lng=${searchLng}&radius=${radius}`
            );

            if (!response.ok) throw new Error(`搜索失败: ${response.status}`);

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

        this.clearMarkers();

        spots.forEach(spot => {
            this.addParkingSpotMarker(spot);
        });

        if (spots.length === 1) {
            setTimeout(() => {
                if (this.infoWindows[0]) {
                    this.infoWindows[0].open(this.map, this.markers[0].getPosition());
                }
            }, 500);
        }

        console.log('显示 ' + spots.length + ' 个停车位');
    }

    /**
     * 添加停车位标记
     */
    addParkingSpotMarker(spot) {
        if (!this.map) return;

        const position = new AMap.LngLat(spot.longitude, spot.latitude);

        const marker = new AMap.Marker({
            position: position,
            map: this.map,
            icon: new AMap.Icon({
                size: new AMap.Size(32, 32),
                image: this.createParkingIconSVG(spot),
                imageSize: new AMap.Size(32, 32)
            }),
            title: spot.title,
            extData: spot
        });

        this.markers.push(marker);

        // 信息窗口
        const infoWindow = this.createInfoWindow(spot);
        this.infoWindows.push(infoWindow);

        marker.on('click', () => {
            this.closeAllInfoWindows();
            infoWindow.open(this.map, marker.getPosition());
        });

        // hover 效果
        marker.on('mouseover', () => {
            marker.setIcon(new AMap.Icon({
                size: new AMap.Size(36, 36),
                image: this.createParkingIconSVG(spot, true),
                imageSize: new AMap.Size(36, 36)
            }));
        });
        marker.on('mouseout', () => {
            marker.setIcon(new AMap.Icon({
                size: new AMap.Size(32, 32),
                image: this.createParkingIconSVG(spot),
                imageSize: new AMap.Size(32, 32)
            }));
        });
    }

    /**
     * 生成停车位图标 SVG（文本转 base64）
     */
    createParkingIconSVG(spot, hover = false) {
        const color = spot.is_available ? '#2ecc71' : '#e74c3c';
        const size = hover ? 36 : 32;
        const price = spot.price_per_hour || 0;

        const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="10" fill="${color}" fill-opacity="${hover ? '0.9' : '0.8'}"/>
            <text x="12" y="16" text-anchor="middle" fill="white" font-size="10" font-weight="bold">¥${price}</text>
        </svg>`;
        return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
    }

    /**
     * 创建信息窗口
     */
    createInfoWindow(spot) {
        const stars = '★'.repeat(Math.floor(spot.avg_rating || 0)) + '☆'.repeat(5 - Math.floor(spot.avg_rating || 0));

        return new AMap.InfoWindow({
            content: `
                <div style="padding:15px;max-width:300px;">
                    <h4 style="margin:0 0 10px;color:#333;">
                        <i class="fas fa-parking"></i> ${spot.title || '停车位'}
                    </h4>
                    <div style="margin-bottom:10px;">
                        <p style="margin:0 0 5px;color:#666;font-size:13px;">
                            <i class="fas fa-map-marker-alt"></i> ${spot.address || '未知地址'}
                        </p>
                        <p style="margin:0 0 5px;color:#666;font-size:13px;">
                            <i class="fas fa-clock"></i> ${spot.price_per_hour}元/小时
                            ${spot.price_per_day ? '(' + spot.price_per_day + '元/天)' : ''}
                        </p>
                    </div>
                    <div style="display:flex;flex-wrap:wrap;gap:5px;margin-bottom:15px;">
                        ${spot.is_available
                            ? '<span style="background:#2ecc71;color:#fff;padding:2px 8px;border-radius:10px;font-size:12px;">可用</span>'
                            : '<span style="background:#e74c3c;color:#fff;padding:2px 8px;border-radius:10px;font-size:12px;">已占用</span>'}
                        ${spot.has_security ? '<span style="background:#4A90D9;color:#fff;padding:2px 8px;border-radius:10px;font-size:12px;">安保</span>' : ''}
                        ${spot.is_covered ? '<span style="background:#9b59b6;color:#fff;padding:2px 8px;border-radius:10px;font-size:12px;">有顶棚</span>' : ''}
                    </div>
                    <div style="display:flex;justify-content:space-between;align-items:center;">
                        <div style="color:#f39c12;font-size:14px;">
                            ${stars}
                            <span style="color:#666;font-size:12px;margin-left:5px;">
                                ${spot.avg_rating ? spot.avg_rating.toFixed(1) : '0.0'}
                            </span>
                        </div>
                        <a href="/parking/${spot.id}" style="
                            display:inline-block;padding:5px 15px;background:#4A90D9;color:#fff;
                            text-decoration:none;border-radius:4px;font-size:12px;
                        " onmouseover="this.style.background='#357ABD'" onmouseout="this.style.background='#4A90D9'">
                            <i class="fas fa-info-circle"></i> 查看详情
                        </a>
                    </div>
                </div>
            `,
            offset: new AMap.Pixel(0, -32)
        });
    }

    /**
     * 清除所有标记
     */
    clearMarkers() {
        if (!this.map) return;
        this.markers.forEach(m => this.map.remove(m));
        this.markers = [];
        this.infoWindows = [];
    }

    /**
     * 关闭所有信息窗口
     */
    closeAllInfoWindows() {
        this.infoWindows.forEach(w => w.close());
    }

    /**
     * 显示/隐藏加载状态
     */
    showLoading(show) {
        const container = document.getElementById(this.containerId);
        if (!container) return;
        let loading = container.querySelector('.map-loading');
        if (show) {
            if (!loading) {
                loading = document.createElement('div');
                loading.className = 'map-loading';
                loading.innerHTML = `<div style="position:absolute;top:0;left:0;right:0;bottom:0;background:rgba(255,255,255,0.8);display:flex;align-items:center;justify-content:center;z-index:1000;">
                    <div style="text-align:center;"><i class="fas fa-spinner fa-spin" style="font-size:30px;color:#4A90D9;margin-bottom:10px;"></i><p style="color:#666;margin:0;">搜索中...</p></div>
                </div>`;
                container.style.position = 'relative';
                container.appendChild(loading);
            }
        } else {
            if (loading) loading.remove();
        }
    }

    /**
     * 显示错误提示
     */
    showError(message) {
        const container = document.getElementById(this.containerId);
        if (!container) return;
        const errorDiv = document.createElement('div');
        errorDiv.className = 'map-error';
        errorDiv.innerHTML = `<div style="position:absolute;top:10px;left:10px;right:10px;background:rgba(231,76,60,0.9);color:#fff;padding:10px 15px;border-radius:4px;z-index:1000;display:flex;align-items:center;justify-content:space-between;font-size:14px;">
            <span><i class="fas fa-exclamation-circle"></i> ${message}</span>
            <button onclick="this.parentElement.remove()" style="background:none;border:none;color:#fff;cursor:pointer;padding:0;margin-left:10px;"><i class="fas fa-times"></i></button>
        </div>`;
        container.style.position = 'relative';
        container.appendChild(errorDiv);
        setTimeout(() => { if (errorDiv.parentElement) errorDiv.remove(); }, 5000);
    }

    /**
     * 绘制搜索范围圆
     */
    drawSearchCenter(centerLng, centerLat, radius) {
        if (!this.map) return;
        if (this.searchCircle) this.map.remove(this.searchCircle);

        this.searchCircle = new AMap.Circle({
            center: new AMap.LngLat(centerLng, centerLat),
            radius: radius,
            strokeColor: '#4A90D9',
            strokeWeight: 2,
            strokeOpacity: 0.5,
            fillColor: '#4A90D9',
            fillOpacity: 0.1
        });
        this.searchCircle.setMap(this.map);
    }

    /**
     * 点击选点
     */
    enableCoordinatePicker(callback) {
        if (!this.map) return;

        this.map.on('click', (e) => {
            const lnglat = e.lnglat;
            const lng = lnglat.getLng();
            const lat = lnglat.getLat();

            if (callback && typeof callback === 'function') {
                callback(lat, lng);
            }

            const infoWindow = new AMap.InfoWindow({
                content: `
                    <div style="padding:10px;max-width:200px;">
                        <h4 style="margin:0 0 5px;color:#4A90D9;"><i class="fas fa-crosshairs"></i> 选择的位置</h4>
                        <p style="margin:0 0 5px;color:#666;font-size:12px;">
                            纬度: ${lat.toFixed(6)}<br>经度: ${lng.toFixed(6)}
                        </p>
                    </div>
                `
            });
            infoWindow.open(this.map, lnglat);
        });
    }

    /**
     * 路线规划
     */
    calculateRoute(startLng, startLat, endLng, endLat) {
        if (!this.map) return;

        AMap.plugin('AMap.Driving', () => {
            const driving = new AMap.Driving({
                map: this.map,
                policy: AMap.DrivingPolicy.LEAST_TIME
            });
            driving.search(
                new AMap.LngLat(startLng, startLat),
                new AMap.LngLat(endLng, endLat),
                (status, result) => {
                    if (status !== 'complete') {
                        this.showError('路线规划失败');
                    }
                }
            );
        });
    }

    /**
     * 销毁地图
     */
    destroy() {
        if (this.map) {
            this.clearMarkers();
            this.map.destroy();
            this.map = null;
        }
    }
}

// 全局地图实例
let map = null;

/**
 * 初始化地图（全局快捷入口）
 */
function initParkingMap(containerId = 'map-container', lat = 39.909186, lng = 116.397389) {
    map = new ParkingMap(containerId);
    map.initMap(lat, lng);
    return map;
}

if (typeof window !== 'undefined') {
    window.ParkingMap = ParkingMap;
    window.initParkingMap = initParkingMap;
    window.map = map;
}

// 自动初始化页面上的地图
document.addEventListener('DOMContentLoaded', function() {
    const containers = document.querySelectorAll('[data-auto-init-map]');
    containers.forEach(container => {
        const id = container.id;
        const lat = parseFloat(container.dataset.lat) || 39.909186;
        const lng = parseFloat(container.dataset.lng) || 116.397389;
        const zoom = parseInt(container.dataset.zoom) || 13;
        if (id) {
            setTimeout(() => {
                const pm = new ParkingMap(id);
                pm.initMap(lat, lng, zoom);
                const params = new URLSearchParams(window.location.search);
                const slat = params.get('lat');
                const slng = params.get('lng');
                const radius = params.get('radius') || 5000;
                if (slat && slng) {
                    pm.searchNearbyParkingSpots(parseFloat(slat), parseFloat(slng), parseInt(radius));
                }
            }, 100);
        }
    });
});
