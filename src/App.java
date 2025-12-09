import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load FXML from file system when running from src directory
        File fxmlFile = new File("src/ui/view/LoginView.fxml");
        Parent root = FXMLLoader.load(fxmlFile.toURI().toURL());
        
        Scene scene = new Scene(root, 500, 400);
        
        // Load CSS from file system
        File cssFile1 = new File("src/ui/app.css");
        File cssFile2 = new File("src/ui/light-theme.css");
        scene.getStylesheets().add(cssFile1.toURI().toURL().toExternalForm());
        scene.getStylesheets().add(cssFile2.toURI().toURL().toExternalForm());
        
        primaryStage.setTitle("Proxy Shopping Management - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
    