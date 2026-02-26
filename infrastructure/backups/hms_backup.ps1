# Hospital Management System - Automated MySQL Backup Script
# Version: 1.1 (Production)
# -------------------------------------------------------------

# --- CONFIGURATION (Adjust for Hospital Server) ---
$BackupFolder = "D:\Hospital_Backups"
$LogFile = "D:\Hospital_Backups\backup_log.txt"
$DatabaseName = "hms_auth"
$RetentionDays = 14
$MySqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin" # Default path - Check on server

# --- PRE-REQUISITES ---
# Ensure the backup folder exists
if (!(Test-Path $BackupFolder)) {
    New-Item -ItemType Directory -Path $BackupFolder -Force | Out-Null
}

$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$BackupFile = "$BackupFolder\hms_backup_$Timestamp.sql"
$LogTimestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

Write-Output "[$LogTimestamp] Backup started for $DatabaseName" | Out-File $LogFile -Append

try {
    # 1. Verify mysqldump existence
    $DumpExe = Join-Path $MySqlPath "mysqldump.exe"
    if (!(Test-Path $DumpExe)) {
        throw "mysqldump.exe not found at $MySqlPath. Please update MySqlPath in the script."
    }

    # 2. Set credentials securely for this session ONLY (Option B: Env)
    # The Task Scheduler should be configured with a restricted environment variable
    # Or, the hms_backup_user can use --login-path after setup.
    # --- PRO-TIP: Run 'mysql_config_editor set --login-path=hms_backup --host=localhost --user=hms_backup_user --password' on the server ---
    
    # 3. Execute mysqldump
    # Note: --single-transaction is vital for clinical data consistency
    # Note: --routines and --triggers are vital for schema integrity
    $Args = @(
        "--login-path=hms_backup",
        "--single-transaction",
        "--triggers",
        "--routines",
        "--hex-blob",
        $DatabaseName,
        "--result-file=$BackupFile"
    )
    
    & $DumpExe $Args 2>> $LogFile
    
    if ($LASTEXITCODE -ne 0) {
        throw "MySQL dump failed with exit code $LASTEXITCODE"
    }

    # 4. Verify results
    $FileSize = (Get-Item $BackupFile).Length
    $SizeMB = [Math]::Round($FileSize / 1MB, 2)
    $EndTimestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Output "[$EndTimestamp] SUCCESS: File: $(Split-Path $BackupFile -Leaf) | Size: $SizeMB MB" | Out-File $LogFile -Append

    # 5. RETENTION POLICY: Delete backups older than N days
    Get-ChildItem -Path $BackupFolder -Filter "*.sql" | Where-Object { 
        $_.LastWriteTime -lt (Get-Date).AddDays(-$RetentionDays) 
    } | Remove-Item -Force -ErrorAction SilentlyContinue
    
    Write-Output "[$EndTimestamp] Cleaned up backups older than $RetentionDays days" | Out-File $LogFile -Append

}
catch {
    $FailTimestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Output "[$FailTimestamp] CRITICAL ERROR: $($_.Exception.Message)" | Out-File $LogFile -Append
    # You could add email notification logic here
    exit 1
}
