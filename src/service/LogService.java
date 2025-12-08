package service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogService {
    private static final LogService instance = new LogService();
    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_LOGS = 1000; // Keep last 1000 log entries

    private LogService() {
        log("INFO", "Log service initialized");
    }

    public static LogService getInstance() {
        return instance;
    }

    /**
     * Log a message with specified level
     */
    public void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);
        
        synchronized (logs) {
            logs.add(logEntry);
            
            // Keep only last MAX_LOGS entries
            if (logs.size() > MAX_LOGS) {
                logs.remove(0);
            }
        }
        
        // Also print to console for debugging
        System.out.println(logEntry);
    }

    /**
     * Log info message
     */
    public void info(String message) {
        log("INFO", message);
    }

    /**
     * Log warning message
     */
    public void warn(String message) {
        log("WARN", message);
    }

    /**
     * Log error message
     */
    public void error(String message) {
        log("ERROR", message);
    }

    /**
     * Get all logs as a single string
     */
    public String getAllLogs() {
        synchronized (logs) {
            if (logs.isEmpty()) {
                return "No logs available.";
            }
            return String.join("\n", logs);
        }
    }

    /**
     * Clear all logs
     */
    public void clearLogs() {
        synchronized (logs) {
            logs.clear();
            log("INFO", "Logs cleared by admin");
        }
    }

    /**
     * Get number of log entries
     */
    public int getLogCount() {
        return logs.size();
    }
}
