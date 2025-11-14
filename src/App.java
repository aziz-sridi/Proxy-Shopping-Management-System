import javafx.application.Application;
import javafx.stage.Stage;
import ui.MainView;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        new MainView().start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
