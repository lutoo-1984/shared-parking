<?php
/**
 * 数据库配置和连接类
 * 使用预处理语句防止SQL注入
 */

class Database {
    private static $instance = null;
    private $connection;
    private $host;
    private $database;
    private $username;
    private $password;
    private $charset;

    /**
     * 私有构造函数，防止直接实例化
     */
    private function __construct() {
        $this->loadConfig();
        $this->connect();
    }

    /**
     * 加载数据库配置
     */
    private function loadConfig() {
        // 优先使用环境变量，否则使用默认值
        $this->host = getenv('DB_HOST') ?: 'localhost';
        $this->database = getenv('DB_DATABASE') ?: 'shared_parking';
        $this->username = getenv('DB_USERNAME') ?: 'root';
        $this->password = getenv('DB_PASSWORD') ?: '';
        $this->charset = 'utf8mb4';
    }

    /**
     * 建立数据库连接
     */
    private function connect() {
        try {
            $dsn = "mysql:host={$this->host};dbname={$this->database};charset={$this->charset}";
            $options = [
                PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES   => false,
                PDO::ATTR_PERSISTENT         => false,
            ];

            $this->connection = new PDO($dsn, $this->username, $this->password, $options);

            // 设置时区为中国时区
            $this->connection->exec("SET time_zone = '+08:00'");

        } catch (PDOException $e) {
            throw new Exception("数据库连接失败: " . $e->getMessage());
        }
    }

    /**
     * 获取数据库单例实例
     */
    public static function getInstance() {
        if (self::$instance === null) {
            self::$instance = new self();
        }
        return self::$instance;
    }

    /**
     * 获取PDO连接实例
     */
    public function getConnection() {
        return $this->connection;
    }

    /**
     * 执行查询并返回所有结果
     */
    public function query($sql, $params = []) {
        try {
            $stmt = $this->connection->prepare($sql);
            $stmt->execute($params);
            return $stmt->fetchAll();
        } catch (PDOException $e) {
            throw new Exception("查询执行失败: " . $e->getMessage());
        }
    }

    /**
     * 执行查询并返回单行结果
     */
    public function querySingle($sql, $params = []) {
        try {
            $stmt = $this->connection->prepare($sql);
            $stmt->execute($params);
            return $stmt->fetch();
        } catch (PDOException $e) {
            throw new Exception("查询执行失败: " . $e->getMessage());
        }
    }

    /**
     * 执行更新/插入/删除操作
     */
    public function execute($sql, $params = []) {
        try {
            $stmt = $this->connection->prepare($sql);
            $stmt->execute($params);
            return $stmt->rowCount();
        } catch (PDOException $e) {
            throw new Exception("操作执行失败: " . $e->getMessage());
        }
    }

    /**
     * 插入数据并返回最后插入的ID
     */
    public function insert($sql, $params = []) {
        try {
            $stmt = $this->connection->prepare($sql);
            $stmt->execute($params);
            return $this->connection->lastInsertId();
        } catch (PDOException $e) {
            throw new Exception("插入操作失败: " . $e->getMessage());
        }
    }

    /**
     * 开始事务
     */
    public function beginTransaction() {
        return $this->connection->beginTransaction();
    }

    /**
     * 提交事务
     */
    public function commit() {
        return $this->connection->commit();
    }

    /**
     * 回滚事务
     */
    public function rollBack() {
        return $this->connection->rollBack();
    }

    /**
     * 检查表是否存在
     */
    public function tableExists($tableName) {
        $sql = "SHOW TABLES LIKE ?";
        $result = $this->query($sql, [$tableName]);
        return count($result) > 0;
    }

    /**
     * 安全地转义字符串（应优先使用预处理语句）
     */
    public function escape($string) {
        return $this->connection->quote($string);
    }

    /**
     * 关闭数据库连接
     */
    public function close() {
        $this->connection = null;
        self::$instance = null;
    }

    /**
     * 防止克隆
     */
    private function __clone() {}

    /**
     * 防止反序列化
     */
    public function __wakeup() {
        throw new Exception("Cannot unserialize a singleton.");
    }
}

// 全局数据库辅助函数

/**
 * 获取数据库实例
 */
function db() {
    return Database::getInstance();
}

/**
 * 快速查询
 */
function db_query($sql, $params = []) {
    return db()->query($sql, $params);
}

/**
 * 快速执行
 */
function db_execute($sql, $params = []) {
    return db()->execute($sql, $params);
}

/**
 * 快速插入
 */
function db_insert($sql, $params = []) {
    return db()->insert($sql, $params);
}