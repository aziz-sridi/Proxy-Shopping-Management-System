# Authentication & Admin System Documentation

## Overview
This system adds user authentication and role-based access control to the Proxy Shopping Management System. Only authenticated users can access the application, and only admins can manage users.

## Database Changes

### New Table: `users`
```sql
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) CHECK (role IN ('admin','user')) DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Roles:**
- `admin`: Full access including user management
- `user`: Standard user access (no user management)

## Initial Setup

### 1. Update Database Schema
Run the updated `sql_schema.sql` to create the users table:
```sql
psql -U aziz -d Shien_java_project -f sql_schema.sql
```

### 2. Create Default Admin User
Run the initialization script to create the default admin account:
```sql
psql -U aziz -d Shien_java_project -f init_admin_user.sql
```

**Default Admin Credentials:**
- Username: `admin`
- Password: `admin123`

**⚠️ IMPORTANT:** Change the default admin password immediately in production!

## Authentication Flow

### Login Screen
1. Application starts with a login screen
2. User enters username and password
3. Credentials are validated against the `users` table
4. On successful login, the main application loads
5. Current user is stored in `AuthService` singleton

### Features

#### For All Users
- Access to Clients, Orders, Shipments, Payments, and Dashboard tabs
- Settings tab for preferences

#### For Admin Users Only
- **Admin tab** with User Management interface
- Create new users with specified roles
- View all users in a table
- Delete users (cannot delete self)
- User information includes:
  - User ID
  - Username
  - Role
  - Creation date

## Components

### Core Classes

#### `model.User`
Represents a user in the system with properties:
- `userId`: Unique identifier
- `username`: Unique username
- `password`: User password (plain text stored - consider hashing in production)
- `role`: Either 'admin' or 'user'
- `createdAt`: Account creation timestamp
- `updatedAt`: Last update timestamp

#### `dao.UserDAO`
Data Access Object for user operations:
- `createUser(User)`: Create new user
- `getUserByUsername(String)`: Fetch user by username
- `getUserById(int)`: Fetch user by ID
- `getAllUsers()`: Get all users (admin only)
- `deleteUser(int)`: Delete user by ID
- `deleteUserByUsername(String)`: Delete user by username
- `updateUser(User)`: Update user password/role
- `usernameExists(String)`: Check username availability

#### `service.AuthService`
Singleton service managing authentication:
- `login(String, String)`: Authenticate user
- `logout()`: Clear current user
- `getCurrentUser()`: Get logged-in user
- `isLoggedIn()`: Check authentication status
- `isAdmin()`: Check if current user is admin
- `createUser(String, String, String)`: Create user (admin only)
- `deleteUser(int)`: Delete user (admin only, cannot delete self)
- `getAllUsers()`: Get all users (admin only)

#### `Controller.LoginController`
Handles login screen:
- Validates input
- Authenticates credentials
- Transitions to main application on success
- Shows error messages on failure

#### `Controller.AdminUsersController`
Manages user creation and deletion:
- Displays all users in a table
- Form to create new users
- Delete buttons for each user
- Validation for username/password requirements
- Error/success messaging

## User Management Interface

### Create User
1. Enter username (minimum 3 characters)
2. Enter password (minimum 4 characters)
3. Select role (admin or user)
4. Click "Create User"
5. Confirmation message appears

### Delete User
1. Locate user in the table
2. Click "Delete" button on the user row
3. Confirm deletion in dialog
4. User is removed from system

**Note:** You cannot delete yourself (current user)

## Security Considerations

### Current Implementation
- Passwords are stored in plain text (⚠️ NOT RECOMMENDED for production)
- Simple string comparison for password validation

### Recommendations for Production
1. **Password Hashing**: Use BCrypt or similar
   ```java
   // Example using Spring Security
   BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
   String hashedPassword = encoder.encode(password);
   ```

2. **Session Management**: Implement JWT tokens or session cookies

3. **Access Control**: Add checks before sensitive operations

4. **Audit Logging**: Log all user creation/deletion

Example production implementation:
```java
// In AuthService.login()
if (user != null && encoder.matches(password, user.getPassword())) {
    currentUser = user;
    return true;
}
```

## File Structure

### New Files
- `src/model/User.java` - User model
- `src/dao/UserDAO.java` - User database operations
- `src/service/AuthService.java` - Authentication service
- `src/Controller/LoginController.java` - Login screen controller
- `src/Controller/AdminUsersController.java` - Admin users controller
- `src/ui/view/LoginView.fxml` - Login screen UI
- `src/ui/view/AdminUsersView.fxml` - Admin users UI
- `init_admin_user.sql` - Default admin creation

### Modified Files
- `sql_schema.sql` - Added users table
- `src/App.java` - Changed entry point to LoginView
- `src/ui/view/MainView.fxml` - Added admin tab
- `src/Controller/MainController.java` - Added admin tab loading logic

## Testing

### Test Scenarios

1. **Login with valid credentials**
   - Username: admin
   - Password: admin123
   - Expected: Main application loads

2. **Login with invalid credentials**
   - Try various wrong passwords
   - Expected: Error message, stay on login screen

3. **Create new user (as admin)**
   - Create user with username and password
   - Expected: User appears in table

4. **Create duplicate username**
   - Try creating user with existing username
   - Expected: Error message

5. **Delete user (as admin)**
   - Select user and click delete
   - Expected: User removed from table

6. **Login as regular user**
   - Create and login as non-admin user
   - Expected: No Admin tab visible, cannot access user management

7. **Access main features**
   - Verify all tabs (Clients, Orders, etc.) are accessible
   - Expected: All features work normally after login

## Logout Feature

Currently, users must close the application to logout. To add a logout button:

1. Add logout button to MainView.fxml
2. Add logout handler in MainController
3. Transition back to LoginView

Example:
```java
@FXML
public void handleLogout() {
    AuthService.getInstance().logout();
    // Load LoginView again
}
```

## Future Enhancements

1. **Password Hashing**: Implement BCrypt for secure password storage
2. **JWT Tokens**: Add session-based authentication
3. **User Roles**: Extend system with more granular permissions
4. **Audit Logging**: Track all user management actions
5. **Password Reset**: Implement password recovery mechanism
6. **Email Verification**: Verify user emails on account creation
7. **Login History**: Track user login timestamps and IP addresses
8. **Two-Factor Authentication**: Add 2FA support

## Troubleshooting

### "Database connection failed" on login
- Verify PostgreSQL is running
- Check database credentials in `DBConnection.java`
- Ensure database and users table exist

### Admin tab not showing
- Verify logged-in user has role='admin' in database
- Check `AuthService.isAdmin()` logic

### Cannot create user
- Verify username is unique and meets requirements (3+ chars)
- Verify password meets requirements (4+ chars)
- Check database write permissions

### Login fails with correct credentials
- Verify password matches exactly (case-sensitive)
- Check user exists in database: `SELECT * FROM users WHERE username='admin';`
