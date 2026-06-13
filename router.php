<?php
/**
 * Router file for PHP built-in development server.
 * Routes all /api/* requests to api/index.php.
 * Routes all web page requests to web/index.php.
 * Serves static files directly.
 */
$uri = $_SERVER['REQUEST_URI'];
$path = parse_url($uri, PHP_URL_PATH);

// Serve static files from web/ directory (css, js, images, etc.)
$staticPaths = ['/assets/', '/favicon.ico'];
$inStatic = false;
foreach ($staticPaths as $prefix) {
    if (strpos($path, $prefix) === 0) { $inStatic = true; break; }
}

if ($inStatic || $path === '/assets' || $path === '/favicon.ico') {
    $webPath = __DIR__ . '/web' . $path;
    if (file_exists($webPath) && is_file($webPath)) {
        // Set correct MIME type
        $ext = pathinfo($webPath, PATHINFO_EXTENSION);
        $mimeTypes = [
            'css' => 'text/css; charset=utf-8',
            'js'  => 'application/javascript; charset=utf-8',
            'svg' => 'image/svg+xml',
            'png' => 'image/png',
            'jpg' => 'image/jpeg',
            'jpeg'=> 'image/jpeg',
            'gif' => 'image/gif',
            'ico' => 'image/x-icon',
            'txt' => 'text/plain; charset=utf-8',
        ];
        if (isset($mimeTypes[$ext])) {
            header('Content-Type: ' . $mimeTypes[$ext]);
        }
        readfile($webPath);
        return true;
    }
}

// Route API requests
if (strpos($path, '/api/') === 0 || $path === '/api') {
    $_SERVER['SCRIPT_NAME'] = '/api/index.php';
    $_SERVER['SCRIPT_FILENAME'] = __DIR__ . '/api/index.php';
    require __DIR__ . '/api/index.php';
    return true;
}

// Route upload requests
if (strpos($path, '/uploads/') === 0) {
    return false; // Let PHP serve the file
}

// All other routes -> web frontend SPA
$_SERVER['SCRIPT_NAME'] = '/web/index.php';
$_SERVER['SCRIPT_FILENAME'] = __DIR__ . '/web/index.php';
require __DIR__ . '/web/index.php';
return true;
