-- SQL Script to create a dedicated backup user for HMS
-- Execute this as root or an admin user in MySQL

CREATE USER IF NOT EXISTS 'hms_backup_user'@'localhost' IDENTIFIED BY 'HospitalBackupPass2026!';

-- Grant minimal required permissions for mysqldump
GRANT SELECT, LOCK TABLES, SHOW VIEW, EVENT, TRIGGER, RELOAD ON *.* TO 'hms_backup_user'@'localhost';

-- Ensure the user cannot perform destructive actions
REVOKE INSERT, UPDATE, DELETE, DROP, CREATE ON *.* FROM 'hms_backup_user'@'localhost';

FLUSH PRIVILEGES;

SELECT 'Backup user created with limited privileges.' AS Status;
