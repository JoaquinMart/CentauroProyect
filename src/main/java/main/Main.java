package main;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        AppController controller = new AppController();
        controller.init(stage);
    }

    // Método para obtener el Stage principal
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
