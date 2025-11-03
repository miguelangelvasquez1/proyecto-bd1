package com.tienda.util;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage mainStage;
    private static double DEFAULT_WIDTH = 1000;
    private static double DEFAULT_HEIGHT = 700;

    public static void setStage(Stage stage) {
        mainStage = stage;
        // Aseguramos que al cerrar la ventana actualizamos la bitácora
        mainStage.setOnCloseRequest(event -> {
            try {
                System.out.println("Closing application, updating access log...");
                SessionManager.handleAppClose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            mainStage.setScene(scene);
            mainStage.setTitle(title);
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Stage openModal(String fxmlPath, String title, boolean modal) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setResizable(true);
            stage.sizeToScene();
            if (modal && mainStage != null) {
                stage.initOwner(mainStage);
                stage.initModality(Modality.WINDOW_MODAL);
            }
            stage.show();
            return stage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}