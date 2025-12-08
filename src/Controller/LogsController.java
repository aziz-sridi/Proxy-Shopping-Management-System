package Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import service.AuthService;
import service.LogService;

import java.net.URL;
import java.util.ResourceBundle;

public class LogsController implements Initializable {

    @FXML
    private TextArea logsTextArea;

    @FXML
    private Label statusLabel;

    private LogService logService = LogService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!AuthService.getInstance().isAdmin()) {
            logsTextArea.setText("Access Denied: Admin privileges required to view logs.");
            return;
        }
        
        loadLogs();
        
        // Auto-refresh every 3 seconds
        startAutoRefresh();
    }

    @FXML
    public void handleRefresh() {
        loadLogs();
        statusLabel.setText("Logs refreshed at " + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        ));
    }

    @FXML
    public void handleClear() {
        logService.clearLogs();
        loadLogs();
        statusLabel.setText("Logs cleared");
    }

    private void loadLogs() {
        String logs = logService.getAllLogs();
        logsTextArea.setText(logs);
        
        // Scroll to bottom to show latest logs
        logsTextArea.setScrollTop(Double.MAX_VALUE);
    }

    private void startAutoRefresh() {
        Thread refreshThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000); // Refresh every 3 seconds
                    Platform.runLater(this::loadLogs);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }
}
