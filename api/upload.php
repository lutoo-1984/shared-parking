<?php
/**
 * 文件上传处理
 * 处理停车位图片、用户头像等文件上传
 */

require_once __DIR__ . '/config/constants.php';
require_once __DIR__ . '/lib/auth.php';

/**
 * 处理图片上传
 */
function handleUpload() {
    header('Content-Type: application/json; charset=utf-8');

    try {
        // 验证登录
        $user = Auth::getCurrentUser();
        if (!$user) {
            throw new Exception(ERROR_AUTHENTICATION, HTTP_UNAUTHORIZED);
        }

        // 验证上传文件
        if (!isset($_FILES['file'])) {
            throw new Exception('未选择文件', HTTP_BAD_REQUEST);
        }

        $file = $_FILES['file'];
        $type = $_POST['type'] ?? 'spot'; // spot 或 avatar

        // 验证上传类型
        if (!in_array($type, ['spot', 'avatar'])) {
            throw new Exception('无效的上传类型', HTTP_BAD_REQUEST);
        }

        // 验证文件错误
        if ($file['error'] !== UPLOAD_ERR_OK) {
            $errorMessages = [
                UPLOAD_ERR_INI_SIZE => '文件超过服务器限制',
                UPLOAD_ERR_FORM_SIZE => '文件超过表单限制',
                UPLOAD_ERR_PARTIAL => '文件仅部分上传',
                UPLOAD_ERR_NO_FILE => '未选择文件',
                UPLOAD_ERR_NO_TMP_DIR => '服务器缺少临时目录',
                UPLOAD_ERR_CANT_WRITE => '文件写入失败',
                UPLOAD_ERR_EXTENSION => '文件上传被扩展阻止',
            ];
            $msg = $errorMessages[$file['error']] ?? '未知上传错误';
            throw new Exception($msg, HTTP_BAD_REQUEST);
        }

        // 验证文件类型
        $finfo = finfo_open(FILEINFO_MIME_TYPE);
        $mimeType = finfo_file($finfo, $file['tmp_name']);
        finfo_close($finfo);

        if (!in_array($mimeType, ALLOWED_IMAGE_TYPES)) {
            throw new Exception('不支持的文件类型，仅支持 JPEG、PNG、GIF', HTTP_BAD_REQUEST);
        }

        // 验证文件大小
        if ($file['size'] > UPLOAD_MAX_SIZE) {
            $maxSizeMB = UPLOAD_MAX_SIZE / 1048576;
            throw new Exception("文件大小不能超过{$maxSizeMB}MB", HTTP_BAD_REQUEST);
        }

        // 生成唯一文件名
        $ext = pathinfo($file['name'], PATHINFO_EXTENSION);
        $ext = strtolower($ext);
        $filename = uniqid() . '_' . bin2hex(random_bytes(8)) . '.' . $ext;

        // 按类型分目录存储
        $subDir = ($type === 'avatar') ? 'avatars' : 'spots';
        $uploadDir = UPLOAD_PATH . '/' . $subDir;

        // 确保目录存在
        if (!is_dir($uploadDir)) {
            mkdir($uploadDir, 0755, true);
        }

        $filePath = $uploadDir . '/' . $filename;

        // 移动文件
        if (!move_uploaded_file($file['tmp_name'], $filePath)) {
            throw new Exception('文件保存失败', HTTP_INTERNAL_ERROR);
        }

        // 构建URL
        $url = APP_URL . '/uploads/' . $subDir . '/' . $filename;

        echo json_encode([
            'success' => true,
            'data' => [
                'url' => $url,
                'filename' => $filename,
                'size' => $file['size'],
                'mime_type' => $mimeType
            ]
        ], JSON_UNESCAPED_UNICODE);

    } catch (Exception $e) {
        $code = $e->getCode() ?: HTTP_INTERNAL_ERROR;
        http_response_code($code);
        echo json_encode([
            'success' => false,
            'error' => [
                'code' => $code,
                'message' => $e->getMessage()
            ]
        ], JSON_UNESCAPED_UNICODE);
    }
}

// 执行上传处理
handleUpload();
