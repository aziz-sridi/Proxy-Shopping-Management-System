package service;

import model.User;
import dao.UserDAO;

import java.sql.SQLException;
import java.util.List;

public class AuthService {
    private static final AuthService instance = new AuthService();
    private final UserDAO userDAO = new UserDAO();
    private final LogService logService = LogService.getInstance();
    private User currentUser;

    private AuthService() {
    }

    public static AuthService getInstance() {
        return instance;
    }

    /**
     * Authenticate user with username and password
     */
    public boolean login(String username, String password) throws SQLException {
        logService.info("Login attempt for user: " + username);
        User user = userDAO.getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            logService.info("Login successful for user: " + username + " (Role: " + user.getRole() + ")");
            return true;
        }
        logService.warn("Login failed for user: " + username);
        return false;
    }

    /**
     * Logout current user
     */
    public void logout() {
        if (currentUser != null) {
            logService.info("User logged out: " + currentUser.getUsername());
        }
        currentUser = null;
    }

    /**
     * Get currently logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return currentUser != null && "admin".equals(currentUser.getRole());
    }

    /**
     * Set current user (for testing or session restoration)
     */
    public void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Create new user (admin only)
     */
    public void createUser(String username, String password, String role) throws SQLException {
        if (!isAdmin()) {
            logService.error("Unauthorized user creation attempt by: " + 
                (currentUser != null ? currentUser.getUsername() : "unknown"));
            throw new SecurityException("Only admins can create users");
        }
        if (userDAO.usernameExists(username)) {
            logService.warn("Failed to create user '" + username + "' - Username already exists");
            throw new IllegalArgumentException("Username already exists");
        }
        User newUser = new User(username, password, role);
        userDAO.createUser(newUser);
        logService.info("User created by admin '" + currentUser.getUsername() + "': " + 
            username + " (Role: " + role + ")");
    }

    /**
     * Delete user (admin only)
     */
    public void deleteUser(int userId) throws SQLException {
        if (!isAdmin()) {
            logService.error("Unauthorized user deletion attempt by: " + 
                (currentUser != null ? currentUser.getUsername() : "unknown"));
            throw new SecurityException("Only admins can delete users");
        }
        if (userId == currentUser.getUserId()) {
            logService.warn("Admin '" + currentUser.getUsername() + "' attempted to delete themselves");
            throw new IllegalArgumentException("Cannot delete yourself");
        }
        
        // Get user info before deletion for logging
        User userToDelete = userDAO.getUserById(userId);
        String deletedUsername = userToDelete != null ? userToDelete.getUsername() : "ID:" + userId;
        
        userDAO.deleteUser(userId);
        logService.info("User deleted by admin '" + currentUser.getUsername() + "': " + deletedUsername);
    }

    /**
     * Get all users (admin only)
     */
    public List<User> getAllUsers() throws SQLException {
        if (!isAdmin()) {
            throw new SecurityException("Only admins can view all users");
        }
        return userDAO.getAllUsers();
    }
}
