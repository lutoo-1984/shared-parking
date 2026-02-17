# 图片资源目录

此目录用于存放网站的所有图片资源。

## 必需图片文件

以下是网站正常运行所需的图片文件：

### 1. 停车位图片
- `parking1.jpg` - 示例停车位图片1 (推荐尺寸: 800x600)
- `parking2.jpg` - 示例停车位图片2 (推荐尺寸: 800x600)
- `parking3.jpg` - 示例停车位图片3 (推荐尺寸: 800x600)

### 2. 用户头像
- `avatar-default.png` - 默认用户头像 (推荐尺寸: 200x200)

### 3. 品牌和图标
- `logo.png` - 网站Logo (推荐尺寸: 200x60)
- `favicon.ico` - 网站图标 (推荐尺寸: 32x32)
- `parking-icon.png` - 地图标记图标 (推荐尺寸: 32x32)

### 4. 其他图片
- `hero-bg.jpg` - 首页背景图 (推荐尺寸: 1920x1080)
- `feature-*.jpg` - 功能特色图片 (推荐尺寸: 400x300)
- `payment-methods.png` - 支付方式图标 (推荐尺寸: 600x100)

## 图片规格要求

### 格式要求
- 使用 JPG 格式用于照片
- 使用 PNG 格式用于图标和透明背景图片
- 使用 WebP 格式用于优化加载速度（可选）

### 尺寸要求
1. **停车位列表图片**: 800x600px
2. **停车位详情图片**: 1200x800px
3. **用户头像**: 200x200px (正方形)
4. **Logo**: 200x60px (保持比例)
5. **图标**: 32x32px 或 64x64px

### 质量要求
- 压缩图片以减小文件大小
- 保持合理的图片质量
- 使用适当的颜色配置文件

## 如何添加图片

### 方法1: 使用在线图库
1. 访问免费图库网站（如 Unsplash、Pexels）
2. 搜索相关关键词（如 "parking lot", "car parking", "garage"）
3. 下载合适尺寸的图片
4. 重命名并放入对应目录

### 方法2: 使用图片编辑工具
1. 使用 Photoshop、GIMP 等工具创建图片
2. 导出为指定格式和尺寸
3. 优化图片大小

### 方法3: 使用占位符
开发阶段可以使用占位符服务：
- `https://via.placeholder.com/800x600`
- `https://picsum.photos/800/600`

## 图片优化建议

### 压缩工具
- [TinyPNG](https://tinypng.com/) - 在线图片压缩
- [ImageOptim](https://imageoptim.com/) - 桌面压缩工具
- [Squoosh](https://squoosh.app/) - Google的在线图片优化工具

### 响应式图片
网站支持响应式图片，建议提供多个尺寸：
- 小屏幕: 400x300px
- 中屏幕: 800x600px
- 大屏幕: 1200x800px

### 懒加载
网站已实现图片懒加载，只需为图片添加 `data-src` 属性：
```html
<img data-src="/assets/images/parking1.jpg" alt="停车位">
```

## 版权注意事项

1. 确保使用的图片拥有合法使用权
2. 遵守图片的许可协议
3. 商业使用时注意版权问题
4. 推荐使用免费商业使用的图库

## 开发阶段

在开发阶段，可以使用以下命令快速创建占位图片：

```bash
# 使用ImageMagick创建占位图片（如果已安装）
convert -size 800x600 xc:#cccccc -pointsize 30 -fill #666666 -gravity center -draw "text 0,0 '停车位图片'" parking1.jpg

# 或使用简单的文本文件作为占位
echo "Placeholder image for parking spot" > parking1.txt
```

## 生产部署

在生产环境中，建议：
1. 使用CDN分发图片
2. 启用HTTP/2 和 Brotli/Gzip压缩
3. 实现图片缓存策略
4. 监控图片加载性能