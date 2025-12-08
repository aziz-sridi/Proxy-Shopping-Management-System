package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.User;
import service.AuthService;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminUsersController implements Initializable {

    @FXML
    private TextField newUsernameField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Button createButton;

    @FXML
    private Label createMessageLabel;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Integer> userIdColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, Timestamp> createdAtColumn;

    @FXML
    private TableColumn<User, Void> actionColumn;

    @FXML
    private Label messageLabel;

    private AuthService authService = AuthService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!authService.isAdmin()) {
            messageLabel.setText("Access denied: Admin privileges required");
            return;
        }

        setupTableColumns();
        loadUsers();
        setupDeleteColumn();
        roleComboBox.getItems().addAll("user", "admin");
        roleComboBox.setValue("user");
    }

    private void setupTableColumns() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    }

    private void setupDeleteColumn() {
        actionColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
                deleteButton.setStyle("-fx-font-size: 11;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().isEmpty()) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    if (user.getUserId() == authService.getCurrentUser().getUserId()) {
                        deleteButton.setDisable(true);
                    } else {
                        deleteButton.setDisable(false);
                    }
                    setGraphic(deleteButton);
                }
            }
        });
    }

    @FXML
    public void handleCreateUser() {
        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            createMessageLabel.setText("Username and password are required");
            createMessageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (username.length() < 3) {
            createMessageLabel.setText("Username must be at least 3 characters");
            createMessageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (password.length() < 4) {
            createMessageLabel.setText("Password must be at least 4 characters");
            createMessageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            authService.createUser(username, password, role);
            createMessageLabel.setText("User created successfully!");
            createMessageLabel.setStyle("-fx-text-fill: green;");
            newUsernameField.clear();
            newPasswordField.clear();
            roleComboBox.setValue("user");
            loadUsers();
        } catch (IllegalArgumentException e) {
            createMessageLabel.setText("Error: " + e.getMessage());
            createMessageLabel.setStyle("-fx-text-fill: red;");
        } catch (SQLException e) {
            createMessageLabel.setText("Database error: " + e.getMessage());
            createMessageLabel.setStyle("-fx-text-fill: red;");
        } catch (SecurityException e) {
            createMessageLabel.setText("Access denied: " + e.getMessage());
            createMessageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void handleDeleteUser(User user) {
        if (user.getUserId() == authService.getCurrentUser().getUserId()) {
            messageLabel.setText("Cannot delete yourself");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete user '" + user.getUsername() + "'?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                authService.deleteUser(user.getUserId());
                messageLabel.setText("User deleted successfully!");
                messageLabel.setStyle("-fx-text-fill: green;");
                loadUsers();
            } catch (SQLException e) {
                messageLabel.setText("Database error: " + e.getMessage());
                messageLabel.setStyle("-fx-text-fill: red;");
            } catch (SecurityException e) {
                messageLabel.setText("Access denied: " + e.getMessage());
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    private void loadUsers() {
        try {
            List<User> users = authService.getAllUsers();
            ObservableList<User> observableUsers = FXCollections.observableArrayList(users);
            usersTable.setItems(observableUsers);
        } catch (SQLException e) {
            messageLabel.setText("Error loading users: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
