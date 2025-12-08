import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ui/view/LoginView.fxml"));
        Scene scene = new Scene(root, 500, 400);
        scene.getStylesheets().add(getClass().getResource("/ui/app.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/ui/light-theme.css").toExternalForm());
        
        primaryStage.setTitle("Proxy Shopping Management - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
    