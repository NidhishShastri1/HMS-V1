@echo off
:: Hospital HMS - Backup Wrapper
:: Run this from Windows Task Scheduler daily at 2:00 AM

set PS_SCRIPT="c:\Users\Aseuro\Desktop\HMS\infrastructure\backups\hms_backup.ps1"

echo Starting HMS Backup Process...
PowerShell -NoProfile -ExecutionPolicy Bypass -File %PS_SCRIPT%

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Backup failed. Check D:\Hospital_Backups\backup_log.txt
    pause
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] Backup completed. Check D:\Hospital_Backups\
timeout /t 5
