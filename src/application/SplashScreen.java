package application;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;

public class SplashScreen extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("splash.fxml"));
            Parent splashRoot = loader.load();
            Scene splashScene = new Scene(splashRoot);

            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setScene(splashScene);
            primaryStage.show();

            ProgressBar progressBar = (ProgressBar) splashScene.lookup("#progressBar");

            Timeline timeline = new Timeline();
            final double[] progress = {0};

            KeyFrame keyFrame = new KeyFrame(Duration.millis(50), e -> {
                progress[0] += 0.01;
                progressBar.setProgress(progress[0]);

                if (progress[0] >= 1) {
                    timeline.stop();

                    Platform.runLater(() -> {
                        boolean granted = showPermissionPopup();
                        if (granted) {
                            boolean access = isRootUser();
                            if (access) {
                                loadDashboard();
                            } else {
                                showError("Access Denied", "You are not running as root.\nPlease run with root privileges.");
                                System.exit(0);
                            }
                        } else {
                            System.exit(0);
                        }
                    });
                }
            });

            timeline.getKeyFrames().add(keyFrame);
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean showPermissionPopup() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("System Access Request");
        alert.setHeaderText("Permission Required");
        alert.setContentText("SecureNetX needs permission to scan services and modify firewall rules.\nDo you want to allow it?");
        ButtonType allow = new ButtonType("Allow");
        ButtonType deny = new ButtonType("Deny");
        alert.getButtonTypes().setAll(allow, deny);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == allow;
    }

    private boolean isRootUser() {
        try {
            Process p = new ProcessBuilder("bash", "-c", "id -u").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String uid = reader.readLine();
            p.waitFor();
            return "0".equals(uid);
        } catch (Exception e) {
            return false;
        }
    }

    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
            Parent mainRoot = loader.load();

            DashboardController controller = loader.getController();
            controller.scanServices();

            Scene mainScene = new Scene(mainRoot);
            primaryStage.setScene(mainScene);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showError(String title, String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
