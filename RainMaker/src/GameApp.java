import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.util.Random;


public class GameApp extends Application {
    static final Point2D windowSize = new Point2D(400, 800);


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        Pane root = new Pane();
        init(root);
        Scene scene = new Scene(root, windowSize.getX(), windowSize.getY(), Color.BLACK);

        //start game loop
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {

            }
        };
        loop.start();

        root.setScaleY(-1);
        stage.setScene(scene);
        stage.setTitle("RainMaker");
        stage.setResizable(false);
        stage.show();

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.LEFT) {
                    System.out.println("Left Arrow: <-");
                }

                if (event.getCode() == KeyCode.RIGHT) {
                    System.out.println("Right Arrow: ->");
                }

                if (event.getCode() == KeyCode.UP) {
                    System.out.println("Up Arrow: ^");
                }

                if (event.getCode() == KeyCode.DOWN) {
                    System.out.println("Down Arrow: v");
                }

            }
        });
    }

    private static void init(Pane root) {


        root.getChildren().clear();

    }

}

class Game extends Pane{

}

interface Updatable {
    public void update();
}

abstract class GameObject extends Group {
    private Point2D coords;

    public GameObject(){

    }

    public GameObject(Point2D coords){
        this.coords = coords;
    }

    public Point2D getCoords(){
        return coords;
    }

    public void setCoords(Point2D coords){
        this.coords = coords;
    }

    public void update() {
        for (Node n : getChildren()) {
            if (n instanceof Updatable)
                ((Updatable) n).update();
        }
    }

    void add(Node node) {
        this.getChildren().add(node);
    }
}



