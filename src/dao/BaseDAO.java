package dao;

import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides common CRUD operations and database utility methods.
 */
public abstract class BaseDAO<T> {

    /**
     * Execute an update operation (INSERT, UPDATE, DELETE).
     * 
     * @param sql The SQL statement to execute
     * @param parameterSetter Consumer to set parameters on the PreparedStatement
     * @throws SQLException if database error occurs
     */
    protected void executeUpdate(String sql, SQLConsumer<PreparedStatement> parameterSetter) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (parameterSetter != null) {
                parameterSetter.accept(ps);
            }
            ps.executeUpdate();
        }
    }

    /**
     * Execute an insert and return the generated ID.
     * 
     * @param sql The SQL INSERT statement
     * @param parameterSetter Consumer to set parameters on the PreparedStatement
     * @return The generated ID
     * @throws SQLException if database error occurs
     */
    protected int executeInsertAndReturnId(String sql, SQLConsumer<PreparedStatement> parameterSetter) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (parameterSetter != null) {
                parameterSetter.accept(ps);
            }
            ps.executeUpdate();
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Insert failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Execute a query that returns a list of results.
     * 
     * @param sql The SQL SELECT statement
     * @param parameterSetter Consumer to set parameters (can be null for no parameters)
     * @param rowMapper Function to map ResultSet to entity
     * @return List of results
     * @throws SQLException if database error occurs
     */
    protected List<T> executeQuery(String sql, SQLConsumer<PreparedStatement> parameterSetter, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (parameterSetter != null) {
                parameterSetter.accept(ps);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rowMapper.map(rs));
                }
            }
        }
        return results;
    }

    /**
     * Execute a query that returns a single result.
     * 
     * @param sql The SQL SELECT statement
     * @param parameterSetter Consumer to set parameters
     * @param rowMapper Function to map ResultSet to entity
     * @return The result or null if not found
     * @throws SQLException if database error occurs
     */
    protected T executeQuerySingle(String sql, SQLConsumer<PreparedStatement> parameterSetter, RowMapper<T> rowMapper) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (parameterSetter != null) {
                parameterSetter.accept(ps);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.map(rs);
                }
            }
        }
        return null;
    }

    /**
     * Execute a query that returns a single value (e.g., COUNT, SUM).
     * 
     * @param sql The SQL statement
     * @param parameterSetter Consumer to set parameters (can be null)
     * @param defaultValue Default value if no result
     * @return The result value
     * @throws SQLException if database error occurs
     */
    protected double executeScalarQuery(String sql, SQLConsumer<PreparedStatement> parameterSetter, double defaultValue) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (parameterSetter != null) {
                parameterSetter.accept(ps);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return defaultValue;
    }

    /**
     * Execute operations within a transaction.
     * 
     * @param operation The operations to execute
     * @throws SQLException if database error occurs
     */
    protected void executeInTransaction(SQLConsumer<Connection> operation) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            operation.accept(conn);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    // Log or handle close exception
                }
            }
        }
    }

    /**
     * Functional interface for mapping ResultSet to entity.
     */
    @FunctionalInterface
    protected interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * Functional interface for operations that throw SQLException.
     */
    @FunctionalInterface
    protected interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }
}
