package axl.itmo.client.gui;

import axl.itmo.client.gui.controller.MainController;
import axl.itmo.client.gui.network.NetworkService;
import axl.itmo.client.gui.util.LocaleManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point of the JavaFX GUI client.
 *
 * Usage:
 *   mvn -pl gui-client javafx:run
 *   or: java -jar gui-client/target/gui-client-1.0-SNAPSHOT.jar
 */
public class GUIClientApp extends Application {

    private static Stage   primaryStage;
    private static long    currentUserId = -1;
    private static String  currentPassword;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle(LocaleManager.getInstance().get("app.title"));
        LocaleManager.getInstance().localeProperty().addListener((obs, old, loc) ->
                stage.setTitle(LocaleManager.getInstance().get("app.title")));
        showLogin();
    }

    public static void showLogin() {
        try {
            Parent root = FXMLLoader.load(
                    GUIClientApp.class.getResource("/axl/itmo/client/gui/fxml/login.fxml"));
            Scene scene = new Scene(root);
            applyStylesheet(scene);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openMainWindow(NetworkService network, String login, String host, int port) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    GUIClientApp.class.getResource("/axl/itmo/client/gui/fxml/main.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 720);
            applyStylesheet(scene);
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);

            MainController ctrl = loader.getController();
            ctrl.initData(network, login, host, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Called by LoginController before openMainWindow to cache the password for PushListener. */
    public static void storePassword(String password) {
        currentPassword = password;
    }

    public static String getPassword() {
        return currentPassword != null ? currentPassword : "";
    }

    public static void setCurrentUserId(long id) { currentUserId = id; }
    public static long getCurrentUserId()         { return currentUserId; }

    public static void applyStylesheet(Scene scene) {
        String css = GUIClientApp.class.getResource("/axl/itmo/client/gui/css/style.css").toExternalForm();
        scene.getStylesheets().add(css);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
