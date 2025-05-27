import java.io.*;
import java.net.*;
import javax.sound.sampled.*;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;
import javafx.scene.chart.*;

public class Client extends Application {
    private static int targetBpm;
    private static int userBpm = 0;
    private static int time = 0;
    private XYChart.Series<Number, Number> target = new XYChart.Series<>();
    private XYChart.Series<Number, Number> user = new XYChart.Series<>();
    private Timeline metro;
    private Timeline update;
    private NumberAxis xAxis;

    public static void main(String[] args) {
        launch(args); // GUI & socket are both started inside start()
    }

    public class Metronome {
        private Clip click;

        public Metronome() throws Exception {
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(
                getClass().getResourceAsStream("/click.wav"))) {
                click = AudioSystem.getClip();
                click.open(ais);
            }
        }

        public void tick() {
            if (click.isRunning()) {
                click.stop();
            }
            click.setFramePosition(0);
            click.start();
        }

        public void update() {
            System.out.println("Update tick: userBpm = " + userBpm);
            target.getData().add(new XYChart.Data<>(time, targetBpm));
            user.getData().add(new XYChart.Data<>(time, userBpm));
            while (userBpm > targetBpm*2-10) {
                if (userBpm > targetBpm*4-10) {
                    userBpm = userBpm/4;
                } else if (userBpm > targetBpm*3-10) {
                    userBpm = userBpm/3
                } else if (userBpm > targetBpm*2) {
                    userBpm = userBpm/2;
                }
            }
            if (time >= xAxis.getUpperBound() - 5) {
                xAxis.setLowerBound(xAxis.getLowerBound() + 15);
                xAxis.setUpperBound(xAxis.getUpperBound() + 15);
            }
            time++;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // === Start Python script ===
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "listen.py");
            pb.inheritIO(); // shows Python output in console
            pb.start();
        } catch (IOException e) {
            System.err.println("Failed to start listen.py");
            e.printStackTrace();
        }

        // === Start socket connection in background thread ===
        new Thread(() -> {
            try {
                Socket socket = null;
                while (socket == null) {
                    try {
                        socket = new Socket("localhost", 12345);
                    } catch (IOException e) {
                        System.out.println("[Waiting] Python server not ready...");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    try {
                        int bpm = (int)Double.parseDouble(line.trim());
                        Platform.runLater(() -> userBpm = bpm);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid BPM received: " + line);
                    }
                }

                in.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // === Set up GUI ===
        Metronome m = new Metronome();
        metro = new Timeline();
        update = new Timeline();

        primaryStage.setTitle("TempoHero");
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        primaryStage.setFullScreen(true);

        VBox box = new VBox(screenBounds.getHeight() / 50);
        box.setAlignment(Pos.CENTER);

        Label title = newLabel("TempoHero", Color.BLACK,
                Font.font("Helvetica", FontWeight.BOLD, FontPosture.ITALIC, screenBounds.getHeight() / 10), box);
        VBox.setVgrow(title, Priority.ALWAYS);

        xAxis = new NumberAxis(0, 30, 1);
        NumberAxis yAxis = new NumberAxis(0, 120, 10);
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        xAxis.setLabel("Time (seconds)");
        yAxis.setLabel("Beats Per Minute");
        target.setName("Target BPM");
        user.setName("Your BPM");
        chart.getData().add(user);
        chart.getData().add(target);
        box.getChildren().add(chart);
        VBox.setVgrow(chart, Priority.ALWAYS);

        HBox bpmBox = new HBox();
        bpmBox.setAlignment(Pos.CENTER);
        box.getChildren().add(bpmBox);
        VBox.setVgrow(bpmBox, Priority.ALWAYS);

        Label bpmLabel = newLabel("BPM: ", Color.BLACK,
                Font.font("Times New Roman", screenBounds.getHeight() / 20), bpmBox);
        HBox.setHgrow(bpmLabel, Priority.ALWAYS);

        TextField enterBpm = new TextField(Integer.toString(targetBpm));
        bpmBox.getChildren().add(enterBpm);
        HBox.setHgrow(enterBpm, Priority.ALWAYS);
        bpmBox.setMaxWidth(screenBounds.getWidth() * 0.8);
        enterBpm.setFont(Font.font("Times New Roman", screenBounds.getHeight() / 30));

        enterBpm.setOnAction(e -> {
            if (enterBpm.getText().equals("")) {
                targetBpm = 0;
            } else {
                targetBpm = Integer.parseInt(enterBpm.getText());
            }

            double millis = 60000.0 / targetBpm;

            if (metro != null) metro.stop();
            metro = new Timeline(new KeyFrame(Duration.millis(millis), ev -> m.tick()));
            metro.setCycleCount(Animation.INDEFINITE);
            metro.play();

            update.stop();
            if (targetBpm > 0) {
                update = new Timeline(new KeyFrame(Duration.millis(1000), ev -> m.update()));
                update.setCycleCount(Animation.INDEFINITE);
                update.play();
                if (targetBpm > 60) {
                    yAxis.setUpperBound(targetBpm+60);
                    yAxis.setLowerBound(targetBpm-60);
                } else {
                    yAxis.setUpperBound(120);
                    yAxis.setLowerBound(0);
                }
            }
        });

        Scene scene = new Scene(box, screenBounds.getWidth() - 50, screenBounds.getHeight() - 100);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Label newLabel(String text, Color color, Font font, Pane parent) {
        Label label = new Label(text);
        label.setTextFill(color);
        label.setFont(font);
        parent.getChildren().add(label);
        return label;
    }
}
