/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mediaplayer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Aniket
 */
public class FXMLDocumentController extends Application implements Initializable {

    private final Image playIcon = new Image(getClass().getResourceAsStream("playbutton.png"));
    private final Image pauseIcon = new Image(getClass().getResourceAsStream("pausebutton.png"));
    public MediaView View;
    public BorderPane pane;
    public HBox boxy;
    public Button play;
    private boolean full, playing;
    private ArrayList<MediaPlayer> medias;
    private int currentIndex;
    private MediaPlayer player;
    public Slider volumeSlider, timeSlider;
    private Duration duration;
    public Label playTime;

    @FXML
    private void fullScreen(ActionEvent e) {
        Stage newStage = (Stage) (((Node) (e.getSource())).getScene().getWindow());
        newStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        full = !full;
        if (full) {
            newStage.setFullScreen(true);
            int x = 0;
            for (Node s : boxy.getChildren()) {
                if (s.equals(timeSlider)) {
                } else {
                    x += ((Region) s).getWidth();
                    x += (((Region) s).getWidth() * 0.3);
                }
            }
            timeSlider.setPrefWidth(newStage.getWidth() - x);
            timeSlider.setMaxWidth(newStage.getWidth() - x);
            timeSlider.setMinWidth(newStage.getWidth() - x);
            View.setFitWidth(pane.getWidth());
            View.setFitHeight(pane.getHeight() - pane.getBottom().prefHeight(-1));

        } else {
            newStage.setFullScreen(false);
            timeSlider.setMaxWidth(129);
            timeSlider.setMinWidth(129);
            timeSlider.setPrefWidth(129);
            View.setFitWidth(pane.getWidth());
            View.setFitHeight(pane.getHeight() - pane.getBottom().prefHeight(-1));

        }
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Which File Would You Like To Play");
        File f = fc.showOpenDialog(null);
        if (f != null) {
            String s = f.toURI().toString();
            player = new MediaPlayer(new Media(s));
            ((Stage) View.getScene().getWindow()).setTitle(f.getAbsolutePath());
            player.setAutoPlay(true);
            medias.add(player);
            currentIndex++;
            for (int x = 0; x < medias.size() - 1; x++) {
                medias.get(x).stop();
            }
            player.setVolume(volumeSlider.getValue() / 100.0);
            View.setFitWidth(pane.getWidth());
            View.setFitHeight(pane.getHeight() - pane.getBottom().prefHeight(-1));
            View.setMediaPlayer(player);
            playing = true;
            player.currentTimeProperty().addListener((ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) -> {
                duration = player.getMedia().getDuration();
                updateValues();
            });
            play.setGraphic(new ImageView(pauseIcon));
        }
    }

    @FXML
    private void PlayPause(ActionEvent e) {
        if (currentIndex < 0) {
            alert(AlertType.ERROR, "Error", "There is no Selected Media File!");
        } else {
            if (playing) {
                play.setGraphic(new ImageView(playIcon));
                playing = false;
                player.pause();
            } else {
                play.setGraphic(new ImageView(pauseIcon));
                playing = true;
                player.play();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pane.setStyle("-fx-background-color: black;");
        boxy.setStyle("-fx-background-color: #bfc2c7;"); // TODO: Use css file
        medias = new ArrayList<>();
        full = false;
        currentIndex = -1;
        play.setGraphic(new ImageView(playIcon));
        playing = false;
        volumeSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (volumeSlider.isValueChanging()) {
                if (player != null) {
                    player.setVolume(volumeSlider.getValue() / 100.0);
                }
            }
        });
        timeSlider.valueProperty().addListener((Observable ov) -> {
            if (timeSlider.isValueChanging()) {
                // multiply duration by percentage calculated by slider position
                if (duration != null && !duration.equals(Duration.UNKNOWN)) {
                    player.seek(duration.multiply(timeSlider.getValue() / 100.0));
                }
            }
        });
    }

    protected void updateValues() {
        if (playTime != null && timeSlider != null && volumeSlider != null && duration != null) {
            Platform.runLater(() -> {
                Duration currentTime = player.getCurrentTime();
                playTime.setText(formatTime(currentTime, duration));
                //System.out.println(formatTime(currentTime, duration));
                timeSlider.setDisable(duration.isUnknown());
                if (!timeSlider.isDisabled() && duration.greaterThan(Duration.ZERO) && !timeSlider.isValueChanging()) {
                    timeSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
                }
                if (!volumeSlider.isValueChanging()) {
                    volumeSlider.setValue((int) Math.round(player.getVolume() * 100));
                }
            });
        }
    }

    private String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;

            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds,
                        durationMinutes, durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d",
                        elapsedMinutes, elapsedSeconds);
            }
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        all = stage;
        stage.setTitle("Media Player");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("media.png")));
        stage.setResizable(false);
        stage.show();
    }

    private Stage all;

    public void alert(AlertType at, String title, String message) {
        Alert al = new Alert(at);
        al.initOwner(all);
        al.setTitle(title);
        al.setHeaderText(message);
        al.showAndWait();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
