# Quick Start Guide - Authentication System

## 📋 Prerequisites
- PostgreSQL running
- Database `Shien_java_project` created
- Java 11+ installed

## 🚀 Quick Setup (3 Steps)

### Step 1: Update Database Schema
```bash
psql -U aziz -d Shien_java_project -f sql_schema.sql
```
This creates the new `users` table.

### Step 2: Create Default Admin User
```bash
psql -U aziz -d Shien_java_project -f init_admin_user.sql
```
This creates the default admin account.

### Step 3: Run the Application
Compile and run the application as usual. The login screen will appear.

## 🔐 Default Credentials
```
Username: admin
Password: admin123
```

## ⚠️ IMPORTANT: Change Default Admin Password!
After first login, create a new admin user with a strong password:
1. Login with admin/admin123
2. Go to the Admin tab
3. Create a new user with role "admin"
4. Delete the default admin account

## 👨‍💼 Admin Tasks

### Create User
1. Click Admin tab
2. Enter username (min 3 chars)
3. Enter password (min 4 chars)
4. Select role (admin or user)
5. Click "Create User"

### Delete User
1. Click Admin tab
2. Find user in table
3. Click "Delete" button
4. Confirm deletion

## 👤 Regular User
- Login with credentials
- Cannot see Admin tab
- Cannot manage users
- Full access to all other features

## 🛠️ Troubleshooting

**Q: Login fails with correct credentials**
- Verify user exists: `SELECT * FROM users WHERE username='admin';`
- Verify password matches exactly

**Q: Cannot create user**
- Username must be unique (no duplicates)
- Username minimum 3 characters
- Password minimum 4 characters

**Q: Admin tab not visible**
- Verify logged-in user has role='admin'
- Query: `SELECT role FROM users WHERE username='your_username';`

**Q: Database connection error**
- Verify PostgreSQL is running
- Check connection string in `src/util/DBConnection.java`
- Verify database exists: `psql -U aziz -l | grep Shien_java_project`

## 📚 Full Documentation
See `AUTHENTICATION_README.md` for complete documentation.

## 🔄 System Flow
```
Application Start
  ↓
Login Screen
  ↓
  ├─ Valid credentials? → YES → Main Application
  │                    → NO → Error message
  ↓
Check User Role
  ├─ Admin? → Admin tab visible + User Management
  ├─ User? → Admin tab hidden
```

## 🎯 Next Steps
1. Login with default admin credentials
2. Test user creation (create a test user)
3. Test login with new user
4. Test user deletion
5. Create your own admin accounts
6. Delete default admin for security

---
Need help? Check `AUTHENTICATION_README.md` for detailed documentation.
