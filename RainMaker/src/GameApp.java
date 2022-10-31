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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.util.Random;


public class GameApp extends Application {
    static final Point2D windowSize = new Point2D(400, 800);


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        Game game = new Game();
        Scene scene = new Scene(game, windowSize.getX(), windowSize.getY(), Color.BLACK);

        //start game loop
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                game.run(now);
            }
        };
        loop.start();

        game.setScaleY(-1);
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


}

class Game extends Pane{
    private Pond pond;
    private Cloud cloud;
    private Helipad pad;

    private int fuel;
    private int water;
    private int score;
    private int time;
    Random rand = new Random();
    Point2D gameSize = new Point2D(rand.nextInt((int) GameApp.windowSize.getX()),
                rand.ints((int) (GameApp.windowSize.getY() / 3),
                        (int) GameApp.windowSize.getY()).findFirst().getAsInt());


    Game(){
        System.out.println(gameSize);
        pond = new Pond();
        cloud = new Cloud(gameSize);
        pad = new Helipad();

        fuel = 25000;

        getChildren().addAll(pond, cloud, pad);
    }

    public void run(long now){

    }

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

abstract class Fixed extends GameObject{
    public Fixed(){
        super();
    }

    public Fixed(Point2D coords){
        super(coords);
    }
}

abstract class Moveable extends GameObject{
    private double direction;
    private double speed;

    public Moveable(){
        super();
    }

    public Moveable(Point2D coords){
        super(coords);
    }

    public abstract void move();
}

class Cloud extends Fixed {

    private double saturation;
    private double radius;
    private double area;
    private double percentage;
    private Circle circle;
    private Text text;

    public Cloud(Point2D coordinates) {
        super(coordinates);
        this.saturation = 0;
        this.percentage = 0;
        this.radius = 50;
        this.area = Math.PI * Math.pow(radius, 2);

        this.circle = new Circle(radius);
        this.text = new Text();
        this.text.setTextAlignment(TextAlignment.CENTER);
        this.text.setText(String.format("%.0f %%", percentage));
        this.circle.setFill(Color.WHITE);
        this.circle.setStroke(Color.BLACK);
        this.circle.setStrokeWidth(2);
        this.circle.setCenterX(coordinates.getX());
        this.circle.setCenterY(coordinates.getY());
        this.text.setX(coordinates.getX() - 10);
        this.text.setY(coordinates.getY() + 10);

        add(circle);
        add(text);
    }

    public double getArea() {
        return area;
    }

    public double getPercentage() {
        return percentage;
    }
}


class Pond extends Fixed {

    private double radius;
    private double area;
    private double percentage;
    private Circle circle;
    private Text text;
    private Random random;

    static Random rand = new Random();
    static Point2D coords = new Point2D(rand.nextInt((int) GameApp.windowSize.getX()),
            rand.ints((int) (GameApp.windowSize.getY() / 3),
                    (int) GameApp.windowSize.getY()).findFirst().getAsInt());

    public Pond() {
        super(coords);
        this.percentage = 0;
        this.radius = 25;
        this.area = Math.PI * Math.pow(radius, 2);

        this.circle = new Circle(radius);
        this.text = new Text();
        this.text.setTextAlignment(TextAlignment.CENTER);
        this.text.setText(String.format("%.0f %%", percentage));
        this.circle.setFill(Color.BLUE);
        this.circle.setStroke(Color.BLACK);
        this.circle.setStrokeWidth(2);
        this.circle.setCenterX(coords.getX());
        this.circle.setCenterY(coords.getY());
        this.text.setX(coords.getX() - 10);
        this.text.setY(coords.getY() + 10);

        add(circle);
        add(text);
    }

    public double getArea() {
        return area;
    }

    public double getPercentage() {
        return percentage;
    }
}


class Helipad extends Fixed {
    Circle padCircle;
    Rectangle padSquare;
    private final int radius;
    private final Point2D padSize;

    private static final Point2D center = new Point2D(
            GameApp.windowSize.getX() / 2,
            GameApp.windowSize.getY() / 8);

    public Helipad() {
        super();
        padSize = new Point2D(100, 100);
        int circlePadding = 10;
        radius = (int) (padSize.getX() / 2 - circlePadding);

        makeCircle();
        makeRectangle();

        add(padCircle);
        add(padSquare);
    }

    private void makeRectangle(){
        padSquare = new Rectangle(
                center.getX() - padSize.getX() / 2,
                center.getY() - padSize.getY() / 2,
                padSize.getX(),
                padSize.getY()
        );
        padSquare.setStroke(Color.YELLOW);
        padSquare.setFill(Color.TRANSPARENT);
    }

    private void makeCircle(){
        padCircle = new Circle(center.getX(), center.getY(), radius);
        padCircle.setStroke(Color.GRAY);
        padCircle.setStrokeWidth(2);
        padCircle.setFill(Color.TRANSPARENT);
    }
}

