# HMS - Authentication & User Management Module

This is Phase 1, Module 1 of the Hospital Management System (HMS), designed for offline LAN use. It features a robust Spring Boot backend paired with a premium React frontend based on Vite. 

## Features

* **3 Strict Roles:** ADMIN, RECEPTION, SUPERVISOR.
* **Security:** Password hashing via BCrypt, session-based auth (max 1 session per user), auto-logout after 20 minutes, force password change on first login.
* **Logging:** Immutable login logs (timestamp, IP, outcome).
* **Account Locking:** Accounts are locked after 3 failed login attempts.

## Prerequisites

1. **Java 17+**
2. **Maven 3.8+**
3. **Node.js 18+**
4. **MySQL 8.0+**

## Database Setup

Initialize MySQL and run the following on your database console:
```sql
CREATE DATABASE IF NOT EXISTS hms_auth;
```

The application is configured to connect to `jdbc:mysql://localhost:3306/hms_auth` using the username `root` and password `root`. 
Please adjust `hms-backend/src/main/resources/application.properties` if your local MySQL environment differs.

## Backend Startup

1. Open a terminal and navigate to the backend directory:
   ```bash
   cd hms-backend
   ```
2. Run the application:
   ```bash
   mvn clean spring-boot:run
   ```
*(Note: A default admin user `admin` with password `Admin@123` is automatically provisioned when the database is empty).*

## Frontend Startup

1. Open a new terminal and navigate to the frontend directory:
   ```bash
   cd hms-frontend
   ```
2. Install dependencies (if not done already):
   ```bash
   npm install
   ```
3. Run the Vite development server:
   ```bash
   npm run dev
   ```
4. Access the web app at `http://localhost:5173`.

## Testing

The backend includes automated test coverage for login mechanisms, password history rules, and security contexts.
To run the automated test suite:
```bash
cd hms-backend
mvn test
```

## Security Posture Note

- No "guest" or "remember me" functionality.
- Credentials only exist encrypted or hashed.
- Only the Admin can reset forgotten passwords or unlock user accounts.
