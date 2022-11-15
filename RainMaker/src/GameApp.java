import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
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
    static final Point2D windowSize = new Point2D(400, 800);


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
                    case B -> game.showBoundaries();
                    case R -> game.reset();
                    case SPACE -> game.saturateCloud();
                }
            }
        });
    }


}

class Game extends Pane{
    private Pond pond;
    private Cloud cloud, cloud2, cloud3;
    private Helicopter helicopter;

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
        int fuel = 25000;
        degreesToRotate = 15;
        frames = 0;
        helicopter = new Helicopter(new Point2D(Helipad.getCenter().getX(),
                Helipad.getCenter().getY()), fuel);

        randomSize();

        pond = new Pond();
        cloud = new Cloud(gameSize);
        randomSize();
        cloud2 = new Cloud(gameSize);
        randomSize();
        cloud3 = new Cloud(gameSize);
        Helipad pad = new Helipad();

        getChildren().addAll(pond, cloud, cloud2, cloud3, pad, helicopter);
    }

    private void randomSize(){
        gameSize = new Point2D(rand.nextInt((int) GameApp.windowSize.getX()),
                rand.ints((int) (GameApp.windowSize.getY() / 3),
                        (int) GameApp.windowSize.getY()).findFirst().getAsInt());
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

    public void showBoundaries() {
        helicopter.toggleBoundaries();
        cloud.toggleBoundaries();
    }

    public void saturateCloud() {
        if(Helicopter.isOn()) {
            if (!Shape.intersect(helicopter.helicopter,
                    cloud.circle).getBoundsInLocal().isEmpty()) {
                if (cloud.getSaturation() < 100) {
                    cloud.saturate();
                    if (cloud.getSaturation() >= 30) {
                        pond.fillPond(cloud.getSaturation() * .05); //5%
                    }
                }
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
    private Point2D cords;

    public GameObject(){
        myTranslation = new Translate();
        myRotation = new Rotate();
        myScale = new Scale();
    }

    public GameObject(Point2D cords){
        myTranslation = new Translate();
        myRotation = new Rotate();
        myScale = new Scale();
        this.cords = cords;
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

    public Point2D getCords(){
        return cords;
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

    public Fixed(Point2D cords){
        super(cords);
    }
}

abstract class Movable extends GameObject{
    double heading;
    double speed;

    public Movable(){
        super();
    }

    public Movable(Point2D cords){
        super(cords);
    }

    public abstract void move();
}

class Cloud extends Fixed {

    private double saturation;
    private Color color;
    private double radius;
    private double area;
    final Circle circle;
    private final GameText text;
    private boolean showBorder;


    public Cloud(Point2D cords) {
        super(cords);
        saturation = 0;
        radius = 50;
        area = Math.PI * Math.pow(radius, 2);
        showBorder = true;

        circle = new Circle(radius);
        text = new GameText(String.format("%.0f %%", saturation));
        color = Color.rgb(255, 255, 255);
        circle.setFill(Color.WHITE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        circle.setCenterX(cords.getX());
        circle.setCenterY(cords.getY());
        text.setFill(Color.BLUE);
        text.setX(cords.getX() - 10);
        text.setY(cords.getY() + 10);
        drawBoundaries();

        add(circle);
        add(text);
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

    private void drawBoundaries(){
        if(showBorder){
            circle.setStroke(Color.GREEN);
        }
        else{
            circle.setStroke(Color.TRANSPARENT);
        }
    }

    public void toggleBoundaries() {
        showBorder = !showBorder;
        drawBoundaries();
    }
}


class Pond extends Fixed {

    private double radius;
    private double area;
    private double percentage;
    private final Circle circle;
    private final GameText text;

    static Random rand = new Random();
    static Point2D cords = new Point2D(
            rand.nextInt((int) GameApp.windowSize.getX()),
            rand.ints((int) (GameApp.windowSize.getY() / 3),
                    (int) GameApp.windowSize.getY()).findFirst().getAsInt());

    public Pond() {
        super(cords);
        percentage = 0;
        radius = 25;
        area = Math.PI * Math.pow(radius, 2);

        circle = new Circle(radius);
        text = new GameText(String.format("%.0f %%", percentage));
        circle.setFill(Color.BLUE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        circle.setCenterX(cords.getX());
        circle.setCenterY(cords.getY());
        text.setFill(Color.WHITE);
        text.setX(cords.getX() - 10);
        text.setY(cords.getY() + 10);

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
    private ImageView padView;

    private static final Point2D center = new Point2D(
            GameApp.windowSize.getX() / 2,
            GameApp.windowSize.getY() / 8);

    public Helipad() {
        super();
        padSize = new Point2D(100, 100);
        int circlePadding = 10;
        radius = (int) (padSize.getX() / 2 - circlePadding);
        init();
    }

    private void init(){
        makeHeliImage();
        makeCircle();
        makeRectangle();
    }

    private void makeHeliImage(){
        padView = new ImageView();

        padView.setImage(new Image("heliport.png"));
        padView.setFitWidth(padSize.getX());
        padView.setPreserveRatio(true);
        padView.setSmooth(true);
        padView.setCache(true);
        padView.setX(center.getX() - padSize.getX() / 2);
        padView.setY(center.getY() - padSize.getY() / 2);
        add(padView);
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

class HeloBody extends Fixed{
    private Circle body, rotorCenter;
    private Rectangle rotorMain;
    private Rectangle leftSkid, rightSkid;
    private Rectangle leftSkidConnectorFront, leftSkidConnectorBack,
                      rightSkidConnectorFront, rightSkidConnectorBack;
    private Rectangle tail;
    private Rectangle tailRotor, tailRotorConnector;
    private Color paint;

    HeloBody(){
        init();
    }

    private void init(){
        paint = Color.ORANGE;
        makeBody();
        makeRotorMain();
        makeTail();
        makeSkids();
        makeTailRotor();
        makeWindow();
        positionPieces();
    }

    private void positionPieces(){
        positionMainBody();
        positionSkids();
        positionRotor();
    }

    private void positionMainBody(){
        rotorMain.setX(-40);
        rotorMain.setY(-50);

        rotorCenter.setCenterX(rotorMain.getX() + rotorMain.getWidth() / 2);
        rotorCenter.setCenterY(rotorMain.getY() + rotorMain.getHeight() / 2);

        tail.setX(-15);
        tail.setY(-140);
    }

    private void positionSkids(){
        leftSkid.setX(-50);
        leftSkid.setY(-65);

        rightSkid.setX(45);
        rightSkid.setY(-65);

        leftSkidConnectorFront.setX(-50);
        leftSkidConnectorFront.setY(0);

        leftSkidConnectorBack.setX(-50);
        leftSkidConnectorBack.setY(-37);

        rightSkidConnectorFront.setX(35);
        rightSkidConnectorFront.setY(0);

        rightSkidConnectorBack.setX(35);
        rightSkidConnectorBack.setY(-37);
    }

    private void positionRotor(){
        tailRotor.setX(20);
        tailRotor.setY(-150);
        tailRotorConnector.setX(5);
        tailRotorConnector.setY(-132.5);
    }

    private void makeBody(){
        body = new Circle();
        body.setRadius(40);
        body.setFill(paint);
        add(body);
    }

    private void makeRotorMain(){
        rotorMain = new Rectangle(80, 30);
        rotorCenter = new Circle(5);

        rotorMain.setFill(paint);
        rotorCenter.setFill(Color.GREY);

        add(rotorMain);
        add(rotorCenter);
    }

    private void makeTail(){
        tail = new Rectangle(30, 90);
        tail.setFill(paint);
        add(tail);
    }

    private void makeSkids() {
        double skidHeight = 90, skidWidth = 5;
        double skidConnHeight = 5, skidConnWidth = 15;

        leftSkid = new Rectangle(skidWidth, skidHeight);
        rightSkid = new Rectangle(skidWidth, skidHeight);
        leftSkid.setFill(paint);
        rightSkid.setFill(paint);

        leftSkidConnectorFront = new Rectangle(skidConnWidth, skidConnHeight);
        leftSkidConnectorBack = new Rectangle(skidConnWidth, skidConnHeight);
        rightSkidConnectorFront = new Rectangle(skidConnWidth, skidConnHeight);
        rightSkidConnectorBack = new Rectangle(skidConnWidth, skidConnHeight);
        leftSkidConnectorFront.setFill(paint);
        leftSkidConnectorBack.setFill(paint);
        rightSkidConnectorFront.setFill(paint);
        rightSkidConnectorBack.setFill(paint);

        add(leftSkid);
        add(rightSkid);
        add(leftSkidConnectorFront);
        add(leftSkidConnectorBack);
        add(rightSkidConnectorFront);
        add(rightSkidConnectorBack);
    }

    private void makeTailRotor(){
        tailRotor = new Rectangle(5, 40);
        tailRotorConnector = new Rectangle(20, 5);
        tailRotor.setFill(paint);
        tailRotorConnector.setFill(paint);
        add(tailRotor);
        add(tailRotorConnector);
    }

    private void makeWindow(){
        Arc window = new Arc(0, 8, 32, 30, 180, 180);
        window.setFill(Color.BLUE);
        add(window);
    }


}

class HeloBlade extends Movable{
    private Rectangle blade;
    private Circle bladeCenter;
    private final Point2D heliCenter;

    HeloBlade(Point2D heliCenter){
        this.heliCenter = heliCenter;
        init();
    }

    private void init(){
        makeBlade();
        positionPieces();
    }

    private void positionPieces() {
        blade.setX(heliCenter.getX());
        blade.setY(heliCenter.getY());

        bladeCenter.setCenterX(blade.getX() + blade.getWidth() / 2);
        bladeCenter.setCenterY(blade.getY() + blade.getHeight() / 2);
    }

    private void makeBlade() {
        blade = new Rectangle(5, 200);
        bladeCenter = new Circle(3, Color.BLACK);
        blade.setFill(Color.GREY);
        add(blade);
        add(bladeCenter);
    }

    @Override
    public void move() {

    }
}

class Helicopter extends Movable implements Updatable {
    Circle body;
    Rectangle headingIndicator;
    Rectangle helicopter;
    int maxSpeed = 10;


    private final Point2D heliSize;
    private static boolean ignitionOn;
    private Point2D heliCenter;
    private final Point2D padCenter;
    private boolean showBorder;
    int fuel;
    GameText fuelText;
    HeloBody helibody;
    HeloBlade heliblade;

    Helicopter(Point2D padCenter, int fuel) {
        super();
        heading = 0;
        speed = 0;
        showBorder = true;
        this.fuel = fuel;
        this.padCenter = padCenter;
        heliSize = new Point2D(80, 80);
        heliCenter = new Point2D(heliSize.getX()/2, heliSize.getY()/2);
        ignitionOn = false;


        makeHelicopter();
        //centerHeli();
        makeBody();
        //makeIndicator();
        initFuel();

    }

    private void initFuel() {
        fuelText = new GameText(String.valueOf(fuel));
        fuelText.setX(heliCenter.getX() + 13);
        fuelText.setY(helibody.getTranslateY() - 105);
        fuelText.setFill(Color.YELLOW);
        add(fuelText);
    }
    private void makeHelicopter() {
        helicopter = new Rectangle(heliSize.getX(), heliSize.getY());
        drawBoundaries();
        //add(helicopter);
    }

    private void drawBoundaries(){
        if(showBorder){
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
        /*
        body = new Circle(
                heliCenter.getX() + heliSize.getX() / 2,
                heliCenter.getY() + heliSize.getY() / 2,
                10
        );
        body.setFill(Color.YELLOW);
        add(body);

         */
        heliCenter = new Point2D(
                padCenter.getX() - heliSize.getX()/2,
                padCenter.getY() - heliSize.getY()/2
        );

        helibody = new HeloBody();
        helibody.setTranslateX(heliCenter.getX() + heliSize.getX() / 2);
        helibody.setTranslateY(heliCenter.getY() + heliSize.getY());

        //heliblade = new HeloBlade(new Point2D(-2.5, -135));
        heliblade = new HeloBlade(new Point2D(-2.5, -145)); //scale .5
        heliblade.setTranslateX(heliCenter.getX() + heliSize.getX() / 2);
        heliblade.setTranslateY(heliCenter.getY() + heliSize.getY());

        helibody.setScaleX(.5);
        helibody.setScaleY(.5);
        heliblade.setScaleX(.5);
        heliblade.setScaleY(.5);

        add(helibody);
        add(heliblade);
    }

    @Override
    public void move() {
        this.translate(0, speed);
        updateFuel();
    }

    private void updateFuel(){
        fuel -= Math.abs(speed * 25); // x25 fuel consumption
        fuelText.setText(String.valueOf(fuel));
        fuelText.setY(helibody.getTranslateY() - 105);
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

    public void toggleBoundaries() {
        showBorder = !showBorder;
        drawBoundaries();
    }

    public int getFuel(){
        return fuel;
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

