import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.stage.Stage;
import javafx.scene.Scene;


public class RainMaker extends Application {
    static final Point2D windowSize = new Point2D(700, 800);


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, windowSize.getX(), windowSize.getY());

        //start game loop
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                
            }
        };
        loop.start();

        stage.setScene(scene);
        stage.setTitle("RainMaker");
        stage.setResizable(false);
        stage.show();
    }

}


