package com.tienda;

import javafx.application.Application;
import javafx.stage.Stage;

import com.tienda.dao.AccessBinnacleDAO;
import com.tienda.util.DatabaseInitializer;
import com.tienda.util.SceneManager;
import com.tienda.util.SessionManager;

public class App extends Application {
    
    private static DatabaseInitializer dbInitializer = new DatabaseInitializer();

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.setStage(primaryStage);
        SceneManager.switchScene("/views/Login.fxml", "Login");
    }
    
    public static void main(String[] args) {

        AccessBinnacleDAO abDao = new AccessBinnacleDAO();
        SessionManager.init(abDao);

        dbInitializer.initializeDatabase();
        launch(args);
    }
}