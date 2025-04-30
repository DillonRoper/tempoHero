import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.shape.Circle;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Priority;
import javafx.util.Duration;


public class Client extends Application {
    private int bpm = 60;
    private Timeline metro;
    public static void main(String[] args) {
        launch(args);
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
                click.stop();           // rewind if still playing
            }
            click.setFramePosition(0);  // rewind to start
            click.start();              // fire
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Metronome m = new Metronome();
        metro = new Timeline();

        primaryStage.setTitle("primaryStage");
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        primaryStage.setFullScreen(true);

        VBox box = new VBox(screenBounds.getHeight()/50);
        box.setId("mainBox");
        box.setAlignment(Pos.CENTER);

        Label title = newLabel("TempoHero", Color.BLACK, Font.font("Helvetica", FontWeight.BOLD, FontPosture.ITALIC, screenBounds.getHeight()/10), box);
        VBox.setVgrow(title, Priority.ALWAYS);

        // <placeholder> Will replace with a graph
        Circle metronome = new Circle(50);
        box.getChildren().add(metronome);
        VBox.setVgrow(metronome, Priority.ALWAYS);
        // <\placeholder>

        HBox bpmBox = new HBox();
        bpmBox.setAlignment(Pos.CENTER);
        box.getChildren().add(bpmBox);
        VBox.setVgrow(bpmBox, Priority.ALWAYS);

        Label bpmLabel = newLabel("BPM: ", Color.BLACK, Font.font("Times New Roman", screenBounds.getHeight()/20), bpmBox);
        HBox.setHgrow(bpmLabel, Priority.ALWAYS);

        TextField enterBpm = new TextField(Integer.toString(bpm));
        bpmBox.getChildren().add(enterBpm);
        HBox.setHgrow(enterBpm, Priority.ALWAYS);
        bpmBox.setMaxWidth(screenBounds.getWidth()*0.8);
        enterBpm.setFont(Font.font("Times New Roman", screenBounds.getHeight()/30));

        enterBpm.setOnAction(e -> {
            int bpm = Integer.parseInt(enterBpm.getText());
            double millis = 60000.0 / bpm;
            if (metro != null) metro.stop();
            metro = new Timeline(new KeyFrame(Duration.millis(millis), ev -> m.tick()));
            metro.setCycleCount(Animation.INDEFINITE);
            metro.play();
        });

        Scene scene = new Scene (
        box, 
        screenBounds.getWidth()-50,
        screenBounds.getHeight()-100
        );
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