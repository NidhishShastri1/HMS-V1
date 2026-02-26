# HMS Infrastructure: Hospital Data Protection Policy (Phase 1)
# -------------------------------------------------------------

## 🛡️ SYSTEM ARCHITECTURE
- **Backup Strategy**: Local Automated MySQL Hot-Backup
- **Retention**: Last 14 Daily Snapshots
- **Storage**: `D:\Hospital_Backups\` (Separate Drive recommended)
- **Primary Tool**: PowerShell + `mysqldump` (Production-safe)

---

## 🛠️ INITIAL SETUP (ONCE ONLY)
1. **Create the Backup User**:
   Open MySQL Workbench or your tool of choice and run the script:
   `C:\Users\Aseuro\Desktop\HMS\infrastructure\backups\mysql_backup_user_setup.sql`

2. **Secure Credentials (Critical)**:
   This ensures we don't store passwords in plain text files. Run this in your command prompt:
   ```cmd
   mysql_config_editor set --login-path=hms_backup --host=localhost --user=hms_backup_user --password
   ```
   **Password**: Provide the one from the SQL script (e.g., `HospitalBackupPass2026!`).

3. **Verify Configuration**:
   Open `hms_backup.ps1` and verify the `MySqlPath` variable correctly points to your MySQL installation bin folder.

---

## 📅 AUTOMATION (WINDOWS TASK SCHEDULER)
Set this up to run automatically every night at **2:00 AM**:

1.  Open **Task Scheduler**.
2.  Click **Create Task** (not Basic Task).
3.  **General**: Name it `HMS_Daily_Backup`. Select "Run whether user is logged in or not" and "Run with highest privileges".
4.  **Triggers**: New -> Daily -> 2:00 AM.
5.  **Actions**: New -> Start a program -> `C:\Users\Aseuro\Desktop\HMS\infrastructure\backups\run_hms_backup.bat`.
6.  **Conditions**: Uncheck "Start the task only if the computer is on AC power".
7.  **Settings**: Check "If the task fails, restart every: 1 hour" (up to 3 times).

---

## 🔄 RESTORE VALIDATION (MONTHLY DRILL)
**DO NOT RESTORE TO THE LIVE DATABASE FOR TESTING.**

1.  **Create Test Database**:
    ```sql
    CREATE DATABASE hms_test_restore;
    ```
2.  **Restore Command**:
    Open Command Prompt and run (use your latest filename):
    ```cmd
    mysql -u hms_backup_user -p hms_test_restore < D:\Hospital_Backups\hms_backup_YYYYMMDD_HHMMSS.sql
    ```
3.  **Integrity Checks**:
    - `SELECT COUNT(*) FROM hms_test_restore.patients;`
    - `SELECT COUNT(*) FROM hms_test_restore.bills;`
    - Verify the last UHID and last grand total matches your production records.

---

## ✅ PRODUCTION CHECKLIST
- [ ] MySQL Backup User created and tested.
- [ ] `mysql_config_editor` setup (No plain text passwords).
- [ ] Backup folder `D:\Hospital_Backups` has restricted permissions.
- [ ] Scheduled Task visible in Windows Task Scheduler.
- [ ] `backup_log.txt` showing successful first run.
- [ ] Restore drill performed on `hms_test_restore` DB.

---

**Note**: This is an offline-only system. For true protection, the `D:\` drive should be physically distinct from the `C:\` drive (OS drive). Ensure the server is connected to an Uninterruptible Power Supply (UPS) for safe transactions.
