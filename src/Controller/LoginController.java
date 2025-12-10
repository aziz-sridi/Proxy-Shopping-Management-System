package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.AuthService;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private AuthService authService = AuthService.getInstance();

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password");
            return;
        }

        try {
            if (authService.login(username, password)) {
                // Login successful, load main application
                loadMainApplication();
            } else {
                errorLabel.setText("Invalid username or password");
                passwordField.clear();
            }
        } catch (Exception e) {
            errorLabel.setText("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMainApplication() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/view/MainView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/ui/app.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/ui/light-theme.css").toExternalForm());

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setTitle("Proxy Shopping Management    please 20 ?");
        stage.setScene(scene);
    }
}
