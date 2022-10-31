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
import javafx.scene.text.Font;
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
                game.run();
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
                    game.updateHeading(-15);
                }

                if (event.getCode() == KeyCode.RIGHT) {
                    game.updateHeading(15);
                }

                if (event.getCode() == KeyCode.UP) {
                    game.updateSpeed(0.1);
                }

                if (event.getCode() == KeyCode.DOWN) {
                    game.updateSpeed(-0.1);
                }
                // 'i' Turns on the helicopter ignition.
                if (event.getCode() == KeyCode.I) {
                    game.toggleIgnition();
                }

                // 'b' [optional] shows bounding boxes around objects.
                if (event.getCode() == KeyCode.B) {
                    game.showBoundries();
                }

                // 'r' Reinitialize the game
                if (event.getCode() == KeyCode.R) {
                    game.reset();
                }

            }
        });
    }


}

class Game extends Pane{
    private Pond pond;
    private Cloud cloud;
    private Helipad pad;
    private Helicopter helicopter;

    private int fuel;
    private int water;
    private int score;
    private int time;
    Random rand = new Random();
    Point2D gameSize = new Point2D(rand.nextInt((int) GameApp.windowSize.getX()),
                rand.ints((int) (GameApp.windowSize.getY() / 3),
                        (int) GameApp.windowSize.getY()).findFirst().getAsInt());


    Game(){
        init();
    }

    public void init(){
        pond = new Pond();
        cloud = new Cloud(gameSize);
        pad = new Helipad();
        helicopter = new Helicopter(new Point2D(Helipad.getCenter().getX(), Helipad.getCenter().getY()));

        fuel = 25000;

        getChildren().addAll(pond, cloud, pad, helicopter);
    }
    public void reset(){
        getChildren().clear();
        init();
    }

    public void run(){
        if(Helicopter.isOn()){
            helicopter.move();
        }
    }

    public void updateHeading(int heading){
        helicopter.updateHeading(heading);
        helicopter.setRotate(helicopter.getHeading() + heading);
    }

    public void updateSpeed(double speed){
        helicopter.updateSpeed(speed);
    }

    public void toggleIgnition() {
        if(Helicopter.isOn()){
            Helicopter.toggleIgnition();
        }
    }

    public void showBoundries() {
        helicopter.toggleBoundries();
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
    double heading;
    double speed;

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
    private GameText text;

    public Cloud(Point2D coordinates) {
        super(coordinates);
        saturation = 0;
        percentage = 0;
        radius = 50;
        area = Math.PI * Math.pow(radius, 2);

        circle = new Circle(radius);
        text = new GameText(String.format("%.0f %%", percentage));
        circle.setFill(Color.WHITE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        circle.setCenterX(coordinates.getX());
        circle.setCenterY(coordinates.getY());
        text.setX(coordinates.getX() - 10);
        text.setY(coordinates.getY() + 10);

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
    private GameText text;
    private Random random;

    static Random rand = new Random();
    static Point2D coords = new Point2D(rand.nextInt((int) GameApp.windowSize.getX()),
            rand.ints((int) (GameApp.windowSize.getY() / 3),
                    (int) GameApp.windowSize.getY()).findFirst().getAsInt());

    public Pond() {
        super(coords);
        percentage = 0;
        radius = 25;
        area = Math.PI * Math.pow(radius, 2);

        circle = new Circle(radius);
        text = new GameText(String.format("%.0f %%", percentage));
        circle.setFill(Color.BLUE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        circle.setCenterX(coords.getX());
        circle.setCenterY(coords.getY());
        text.setX(coords.getX() - 10);
        text.setY(coords.getY() + 10);

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
        add(padSquare);
    }

    private void makeCircle(){
        padCircle = new Circle(center.getX(), center.getY(), radius);
        padCircle.setStroke(Color.GRAY);
        padCircle.setStrokeWidth(2);
        padCircle.setFill(Color.TRANSPARENT);
        add(padCircle);
    }

    public static Point2D getCenter(){
        return center;
    }
}

class Helicopter extends Moveable implements Updatable {
    Circle body;
    Rectangle headingIndicator;
    Rectangle helicopter;


    private final Point2D heliSize;
    private static boolean ignitionOn;
    private Point2D heliCenter;
    private final Point2D padCenter;
    private boolean showHelicopter;

    Helicopter(Point2D padCenter) {
        super();
        heading = 0;
        speed = 0;
        showHelicopter = true;
        this.padCenter = padCenter;
        heliSize = new Point2D(80, 80);
        heliCenter = new Point2D(heliSize.getX()/2, heliSize.getY()/2);
        ignitionOn = false;

        makeHelicopter();
        centerHeli();
        makeBody();
        makeIndicator();
    }
    private void makeHelicopter() {
        helicopter = new Rectangle(heliSize.getX(), heliSize.getY());
        drawBoundries();
        add(helicopter);
    }

    private void drawBoundries(){
        if(showHelicopter){
            helicopter.setStroke(Color.GREEN);
            helicopter.setFill(Color.TRANSPARENT);
        }
        else{
            helicopter.setStroke(Color.TRANSPARENT);
            helicopter.setFill(Color.TRANSPARENT);
        }
    }

    private void centerHeli() {
        heliCenter = new Point2D(
                padCenter.getX() - heliSize.getX()/2,
                padCenter.getY() - heliSize.getY()/2
        );
        helicopter.setX(heliCenter.getX());
        helicopter.setY(heliCenter.getY());
    }

    private void makeIndicator() {
        headingIndicator = new Rectangle(
                body.getCenterX() - 2,
                body.getCenterY(),
                5,
                40
        );
        headingIndicator.setFill(Color.YELLOW);
        add(headingIndicator);
    }

    private void makeBody(){
        body = new Circle(
                heliCenter.getX() + heliSize.getX() / 2,
                heliCenter.getY() + heliSize.getY() / 2,
                10);
        body.setFill(Color.YELLOW);
        add(body);
    }

    @Override
    public void move() {
        //helicopter.setRotate(heading);
    }

    private void turn(){
        headingIndicator = new Rectangle(
                body.getCenterX() - 2 + heading,
                body.getCenterY() + heading,
                5,
                40
        );
    }

    public void updateHeading(int update){
        heading -= update;
    }

    public void updateSpeed(double s){
        speed += s;
    }
    public static boolean isOn() {
        return ignitionOn;
    }

    public static void toggleIgnition() {
        ignitionOn = !ignitionOn;
    }

    public double getHeading(){
        return this.heading;
    }


    public void toggleBoundries() {
        showHelicopter = !showHelicopter;
        drawBoundries();
    }
}

class GameText extends GameObject {
    Text text;

    public GameText(String textString) {
        text = new Text(textString);
        text.setScaleY(-1);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFont(Font.font(20));
        add(text);
    }

    public void setX(double x){
        text.setX(x);
    }

    public void setY(double y){
        text.setY(y);
    }
}

