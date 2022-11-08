import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.util.Random;


public class GameApp extends Application {
    static final Point2D windowSize = new Point2D(600, 800);


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        Game game = new Game();
        Scene scene = new Scene(game, windowSize.getX(), windowSize.getY(), Color.BLACK);


        game.setScaleY(-1);
        stage.setScene(scene);
        stage.setTitle("RainMaker");
        stage.setResizable(false);
        stage.show();

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case LEFT -> game.left();
                    case RIGHT -> game.right();
                    case UP -> game.updateSpeed(0.1);
                    case DOWN -> game.updateSpeed(-0.1);
                    case I -> game.toggleIgnition();
                    case B -> game.showBoundries();
                    case R -> game.reset();
                    case SPACE -> game.saturateCloud();
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
    private int frames;
    private int degreesToRotate;
    Random rand = new Random();
    Point2D gameSize = new Point2D(rand.nextInt((int) GameApp.windowSize.getX()),
            rand.ints((int) (GameApp.windowSize.getY() / 3),
                    (int) GameApp.windowSize.getY()).findFirst().getAsInt());


    Game(){
        init();

        //start game loop
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                frames++;
                run();
            }
        };
        loop.start();
    }

    private void run(){
        if(helicopter.getFuel() <= 0){
            reset();
        }
        if(Helicopter.isOn()){
            helicopter.move();
            helicopter.update();
            if((int) cloud.getSaturation() > 0 && frames % 150 == 0){
                cloud.desaturate();
            }
        }
    }

    private void init(){
        fuel = 25000;
        degreesToRotate = 15;
        frames = 0;
        helicopter = new Helicopter(new Point2D(Helipad.getCenter().getX(),
                Helipad.getCenter().getY()), fuel);

        gameSize = new Point2D(rand.nextInt((int) GameApp.windowSize.getX()),
                rand.ints((int) (GameApp.windowSize.getY() / 3),
                        (int) GameApp.windowSize.getY()).findFirst().getAsInt());

        pond = new Pond();
        cloud = new Cloud(gameSize);
        pad = new Helipad();

        getChildren().addAll(pond, cloud, pad, helicopter);
    }

    public void reset(){
        getChildren().clear();
        init();
    }

    public void left(){
        helicopter.updateHeading(-degreesToRotate);
        helicopter.rotate(helicopter.getMyRotation() - degreesToRotate);
        helicopter.update();
    }

    public void right(){
        helicopter.updateHeading(degreesToRotate);
        helicopter.rotate(helicopter.getMyRotation() + degreesToRotate);
        helicopter.update();
    }

    public void updateSpeed(double speed){
        helicopter.updateSpeed(speed);
    }

    public void toggleIgnition() {
        Helicopter.toggleIgnition();
    }

    public void showBoundries() {
        helicopter.toggleBoundries();
    }

    public void saturateCloud() {
        //if over cloud
        System.out.print("\nCloud: " + cloud.getSaturation());
        if(cloud.getSaturation() < 100){
            cloud.saturate();
            if(cloud.getSaturation() >= 30){
                pond.fillPond(cloud.getSaturation() * .05); //5% of sat
            }
        }
    }
}

interface Updatable {
    public void update();
}

abstract class GameObject extends Group {
    protected Translate myTranslation;
    protected Rotate myRotation;
    protected Scale myScale;
    private Point2D coords;

    public GameObject(){
        myTranslation = new Translate();
        myRotation = new Rotate();
        myScale = new Scale();
    }

    public GameObject(Point2D coords){
        myTranslation = new Translate();
        myRotation = new Rotate();
        myScale = new Scale();
        this.coords = coords;
    }


    public void rotate(double degrees) {
        myRotation.setAngle(degrees);
        myRotation.setPivotX(0);
        myRotation.setPivotY(0);
        setRotate(-degrees);
    }

    public void scale(double sx, double sy) {
        myScale.setX(sx);
        myScale.setY(sy);
    }

    public void translate(double tx, double ty) {
        myTranslation.setX(myTranslation.getX() + tx);
        myTranslation.setY(myTranslation.getY() + ty);
    }

    public double getMyRotation(){
        return myRotation.getAngle();
    }

    public Point2D getCoords(){
        return coords;
    }


    public void update() {
        this.getTransforms().clear();
        this.getTransforms().addAll(myTranslation);
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
    private Color color;
    private double radius;
    private double area;
    private final Circle circle;
    private final GameText text;

    public Cloud(Point2D coords) {
        super(coords);
        saturation = 0;
        radius = 50;
        area = Math.PI * Math.pow(radius, 2);

        circle = new Circle(radius);
        text = new GameText(String.format("%.0f %%", saturation));
        color = Color.rgb(255, 255, 255);
        circle.setFill(Color.WHITE);
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

    public double getSaturation(){
        return saturation;
    }


    public void saturate() {
        if(saturation < 100){
            saturation += 1;
            text.setText(String.format("%.0f %%", saturation));
            color = Color.rgb(
                    (int) (255 * color.getRed() - 1),
                    (int) (255 * color.getGreen() - 1),
                    (int) (255 * color.getBlue() - 1 )
            );
            circle.setFill(color);
        }
    }

    public void desaturate() {
        if(saturation > 0){
            saturation -= 1;
            text.setText(String.format("%.0f %%", saturation));
            color = Color.rgb(
                    (int) (255 * color.getRed() + 1),
                    (int) (255 * color.getGreen() + 1),
                    (int) (255 * color.getBlue() + 1 )
            );
            circle.setFill(color);
        }
    }
}


class Pond extends Fixed {

    private double radius;
    private double area;
    private double percentage;
    private Circle circle;
    private final GameText text;
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

    public void fillPond(double fillRate) {
        percentage += fillRate;
        text.setText(String.format("%.0f %%", percentage));
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
    int maxSpeed = 10;


    private final Point2D heliSize;
    private static boolean ignitionOn;
    private Point2D heliCenter;
    private final Point2D padCenter;
    private boolean showHelicopter;
    int fuel;
    GameText fuelText;

    Helicopter(Point2D padCenter, int fuel) {
        super();
        heading = 0;
        speed = 0;
        showHelicopter = true;
        this.fuel = fuel;
        this.padCenter = padCenter;
        heliSize = new Point2D(80, 80);
        heliCenter = new Point2D(heliSize.getX()/2, heliSize.getY()/2);
        ignitionOn = false;

        makeHelicopter();
        centerHeli();
        makeBody();
        makeIndicator();
        initFuel();
    }

    private void initFuel() {
        fuelText = new GameText(String.valueOf(fuel));
        fuelText.setX(heliCenter.getX() + 13);
        fuelText.setY(heliCenter.getY() + 20);
        fuelText.setFill(Color.YELLOW);
        add(fuelText);
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
        this.translate(0, speed);
        updateFuel();
    }

    private void updateFuel(){
        fuel -= Math.abs(speed * 25); // x25 fuel consumption
        fuelText.setText(String.valueOf(fuel));
        fuelText.setY(helicopter.getTranslateY() + heliSize.getY());
    }

    public void updateHeading(int update){
        heading -= update;
    }

    public void updateSpeed(double s){
        if(ignitionOn && Math.abs(speed) < maxSpeed){
            speed += s;
        }
    }
    public static boolean isOn() {
        return ignitionOn;
    }

    public static void toggleIgnition() {
        ignitionOn = !ignitionOn;
        System.out.println("Ignition on: " + ignitionOn);
    }

    public double getHeading(){
        return this.heading;
    }


    public void toggleBoundries() {
        showHelicopter = !showHelicopter;
        drawBoundries();
    }

    public int getFuel(){
        return fuel;
    }

    public double getSpeed(){
        return speed;
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
        this.getTransforms().addAll(myTranslation,myRotation,myScale);
    }

    public void setFill(Color color){
        text.setFill(color);
    }

    public void setText(String textString){
        text.setText(textString);
    }

    public void setX(double x){
        text.setX(x);
    }

    public void setY(double y){
        text.setY(y);
    }

}

