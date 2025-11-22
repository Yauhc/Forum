-- users テーブルに created_at カラムを追加（存在しない場合）
-- created_at 列がなければ追加
SET @need_created_at := (
    SELECT COUNT(*) = 0
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'created_at'
);
SET @ddl_created_at := IF(
    @need_created_at,
    'ALTER TABLE users ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',
    'SELECT 1'
);
PREPARE stmt_created_at FROM @ddl_created_at;
EXECUTE stmt_created_at;
DEALLOCATE PREPARE stmt_created_at;

-- updated_at 列がなければ追加
SET @need_updated_at := (
    SELECT COUNT(*) = 0
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'updated_at'
);
SET @ddl_updated_at := IF(
    @need_updated_at,
    'ALTER TABLE users ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
    'SELECT 1'
);
PREPARE stmt_updated_at FROM @ddl_updated_at;
EXECUTE stmt_updated_at;
DEALLOCATE PREPARE stmt_updated_at;

-- role 列がなければ追加
SET @need_role := (
    SELECT COUNT(*) = 0
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'role'
);
SET @ddl_role := IF(
    @need_role,
    'ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT ''USER''',
    'SELECT 1'
);
PREPARE stmt_role FROM @ddl_role;
EXECUTE stmt_role;
DEALLOCATE PREPARE stmt_role;

