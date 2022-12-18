import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class GameApp extends Application {
    static final Point2D windowSize = new Point2D(1000, 1000);


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        Game game = new Game();
        Scene scene = new Scene(game, windowSize.getX(), windowSize.getY(), Color.BLACK);


        game.setScaleY(-1);
        stage.setScene(scene);
        stage.setTitle("RainMaker");
        //stage.setResizable(false);
        stage.show();

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT -> game.moveLeft();
                case RIGHT -> game.moveRight();
                case UP -> game.moveForward();
                case DOWN -> game.moveBackward();
                case I -> game.toggleIgnition();
                case B -> game.showBoundaries();
                case R -> game.reset();
                case SPACE -> game.saturateCloud();
                case D -> game.showLines();
            }
        });
    }
}

class Game extends Pane{
    private final ArrayList<Cloud> clouds = new ArrayList<>();
    private final ArrayList<Pond> ponds = new ArrayList<>();
    private final ArrayList<Lines> lines = new ArrayList<>();
    private Pond pond;
    private Helicopter helicopter;
    private Helipad helipad;
    private int frames;
    private final Random rand = new Random();
    private Point2D gameSize;
    static double WIND_SPEED = 1;
    static double WIND_DIRECTION = 45;


    Game(){
        init();
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                frames++;
                run();
            }
        };
        loop.start();
    }

    //Initialized the Game object
    private void init(){
        int fuel = 25000;
        frames = 0;

        randomSize();
        makeBackground();

        createPonds();
        createClouds();
        createHelipad();
        createHelicopter(fuel);
        createLines();
    }

    //main logic for running the game
    private void run(){
        updateHelicopter();
        updateClouds();
        updateLines();

        checkWinCondition();
        checkLossCondition();

        if(helicopter.getState() instanceof Ready){
            updateObjects();
        }
    }

    //Create a win window
    private void createWinWindow(){
        Stage stage = new Stage();
        VBox root = new VBox();
        Scene scene = new Scene(root, 600, 550);
        stage.setScene(scene);
        stage.setTitle("You win!");
        stage.show();

        HBox buttons = new HBox();

        Button yes = new Button("Yes");
        Button no = new Button("No");

        Label overText = new Label("Restart the game?");
        Font font = Font.font("Courier New", FontWeight.BOLD, 24);
        overText.setFont(font);

        yes.setOnAction(e -> stage.close());
        no.setOnAction(e -> Platform.exit());

        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(40);
        yes.setFont(font);
        no.setFont(font);
        buttons.getChildren().addAll(overText, yes, no);

        root.getChildren().add(buttons);

        BackgroundImage bg = new BackgroundImage(
                new Image("win.jpg"),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT
        );
        root.setBackground(new Background(bg));
    }

    //If the ponds are full and the helicopter is back safely
    private void checkWinCondition(){
        if(helicopter.getFuel() > 0
                && pond.isFull()
                && helicopter.getState() instanceof Off
                && !Shape.intersect(helicopter.getBorder(),
                    helipad.padBorder()).getBoundsInLocal().isEmpty()){
            createWinWindow();
            reset();
        }
    }

    //If the helicopter runs out of fuel
    private void checkLossCondition(){
        if(helicopter.getFuel() <= 0){
            createLossWindow();
            reset();
        }
    }

    //Create a Loss window
    private void createLossWindow(){
        Stage stage = new Stage();
        VBox root = new VBox();
        Scene scene = new Scene(root, 600, 550);
        stage.setScene(scene);
        stage.setTitle("Game over");
        stage.show();

        HBox buttons = new HBox();

        Button yes = new Button("Yes");
        Button no = new Button("No");

        Label overText = new Label("Restart the game?");
        Font font = Font.font("Courier New", FontWeight.BOLD, 24);
        overText.setFont(font);

        yes.setOnAction(e -> stage.close());
        no.setOnAction(e -> Platform.exit());

        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(40);
        yes.setFont(font);
        no.setFont(font);
        buttons.getChildren().addAll(overText, yes, no);

        root.getChildren().add(buttons);

        BackgroundImage bg = new BackgroundImage(
                new Image("gameover.png"),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT
        );
        root.setBackground(new Background(bg));
    }

    //Updates all of the cloud movements
    private void updateClouds(){
        for(Cloud cloud : clouds){
            //if the cloud is offscreen, it is dead
            if(cloud.getTranslateX()
                    > GameApp.windowSize.getX() + cloud.getSize() * 4){
                cloud.changeState(new CloudDead(cloud));
            }
            cloud.move();
        }
    }

    //updates all of the helicopter movements
    private void updateHelicopter(){
        helicopter.move(); //updates the helicopter with inputs
    }

    //Updates all of the cloud objects as the helicopter takes water
    private void updateObjects(){
        for(Cloud cloud : clouds){
            if((int) cloud.getSaturation() > 0 && frames % 150 == 0){
                cloud.desaturate();
            }
        }
    }

    private void updateLines(){
        for(Lines line : lines){
            line.update();
        }
    }

    //Creates Cloud objects and stores them in ArrayList
    private void createClouds(){
        for(int i = 0; i < 4; i++){
            createCloud();
        }
    }

    private void createCloud(){
        Cloud cloud = new Cloud(WIND_SPEED, WIND_DIRECTION);
        clouds.add(cloud);
        getChildren().add(cloud);
        randomSize();
    }

    //Creates Pond object
    private void createPonds(){
        for(int i = 0; i < 3; i++){
            createPond();
        }
    }

    //Creates Pond objects
    private void createPond(){
        pond = new Pond(gameSize);
        ponds.add(pond);
        getChildren().add(pond);
        randomSize();
    }

    //Creates Helipad Object
    private void createHelipad(){
        helipad = new Helipad();
        getChildren().add(helipad);
    }

    //Creates Helicopter Object
    private void createHelicopter(int fuel){
        helicopter = new Helicopter(new Point2D(Helipad.getCenter().getX(),
                Helipad.getCenter().getY()), fuel);
        getChildren().add(helicopter);
    }

    //Creates a new line between cloud and pond objects
    private void createLines(){
        for(Cloud cloud : clouds){
            for(Pond pond : ponds)
                lines.add(new Lines(cloud, pond));
        }
        for(Lines line : lines){
            getChildren().add(line);
        }
    }

    //Used to create a random gameSize for object placement
    //Creates random point
    private void randomSize(){
        int buffer = 50; //buffer border for object spawns
        gameSize = new Point2D(
                rand.nextInt(
                        buffer,
                        (int) (GameApp.windowSize.getX() - buffer)
                ),
                rand.nextInt(
                        (int) GameApp.windowSize.getY() / 3,
                        (int) GameApp.windowSize.getY() - buffer
                )
        );
    }

    //resets the game world for new game
    public void reset(){
        getChildren().clear();
        clouds.clear();
        ponds.clear();
        lines.clear();
        init();
    }

    //Moves helicopter object to the left on key-press
    public void moveLeft(){
        helicopter.rotateLeft();
    }

    //Moves helicopter object to the right on key-press
    public void moveRight(){
        helicopter.rotateRight();
    }

    //Moves helicopter object forward on key-press
    public void moveForward(){
        helicopter.moveForward();
    }

    //Moves helicopter object backwards on key-press
    public void moveBackward(){
        helicopter.moveBackward();
    }

    //Turns on the helicopter
    public void toggleIgnition() {
        helicopter.toggleIgnition();
    }

    //On 'B' press, shows the boundaries of game objects
    public void showBoundaries() {
        helicopter.toggleBoundaries();
        for(Cloud cloud : clouds){
            cloud.toggleBoundaries();
        }
    }

    //Toggles the lines between objects on and off
    public void showLines(){
        for(Lines line : lines){
            line.toggleVisibility();
        }
    }

    //If the helicopter is on and pressing space, decreases cloud
    //saturation and fills the pond
    public void saturateCloud() {
        if(helicopter.getState() instanceof Ready) {
            for(Cloud cloud : clouds){
                if(!Shape.intersect(helicopter.getBorder(),
                        cloud.getBorder()).getBoundsInLocal().isEmpty()
                        && cloud.getSaturation() < 100) {
                        cloud.saturate();
                    if(cloud.getSaturation() >= 30) { //5% fill rate
                        pond.fillPond(cloud.getSaturation() * .05);
                    }
                }
            }
        }
    }

    //Creates a scalable background from a png
    private void makeBackground(){
        //https://opengameart.org/node/10024
        //image is 500x500 and repeats seamlessly
        Image backgroundImage = new Image("background.png");
        BackgroundImage bg = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        super.setBackground(new Background(bg));
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
        this.getTransforms().addAll(myTranslation, myRotation, myScale);
    }

    public GameObject(Point2D cords){
        myTranslation = new Translate();
        myRotation = new Rotate();
        myScale = new Scale();
        this.cords = cords;
        this.getTransforms().addAll(myTranslation, myRotation, myScale);
    }

    public void rotate(double degrees) {
        myRotation.setAngle(degrees);
        myRotation.setPivotX(0);
        myRotation.setPivotY(0);
    }

    public void scale(double sx, double sy) {
        myScale.setX(sx);
        myScale.setY(sy);
    }

    public void translate(double tx, double ty) {
        myTranslation.setX(tx);
        myTranslation.setY(ty);
    }

    public double getMyRotation(){
        return myRotation.getAngle();
    }

    public Point2D getCords(){
        return cords;
    }

    public void update(){
        for(Node n : getChildren()){
            if(n instanceof Updatable)
                ((Updatable)n).update();
        }
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

class Cloud extends Movable {
    private double saturation;
    private Color color;
    private final double radius;
    private Ellipse cloud;
    private GameText text;
    private boolean showBorder;
    private Rectangle border;
    private CloudState state;
    private final Random rand = new Random();
    private Point2D spawn;


    public Cloud(double speed, double heading) {
        super();
        this.speed = speed;
        this.heading = heading;
        saturation = 0;
        radius = 50;
        showBorder = false;
        spawn = new Point2D(0, 0);
        state = new CloudAlive(this);

        randomSpawn();
        initCloud();
        makeBorder();
    }

    private void initCloud(){
        cloud = new Ellipse(
                spawn.getX(), //clouds now spawn at x = 0
                spawn.getY(),
                radius * 1.65, //how long a cloud is
                radius  //how tall a cloud is
        );
        text = new GameText(String.format("%.0f %%", saturation));
        color = Color.rgb(255, 255, 255);
        cloud.setFill(Color.WHITE);
        cloud.setStroke(Color.BLACK);
        cloud.setStrokeWidth(2);

        text.setFill(Color.BLUE);
        text.setX(spawn.getX() - 10);
        text.setY(spawn.getY() + 10);

        randomizeSpeed();
        add(cloud);
        add(text);
    }

    private void randomSpawn(){
        int oldY = (int) spawn.getY();
        spawn = new Point2D(
                -radius * 2,
                rand.nextInt(
                        250,
                        500
                )
        );
        if((spawn.getY() - oldY) < 50){
            randomSpawn();
        }
    }

    private void randomizeSpeed(){
        speed = ThreadLocalRandom.current().nextDouble(.5, 1.5);
    }

    private void makeBorder(){
        border = new Rectangle(
                cloud.getCenterX() - cloud.getRadiusX(),
                cloud.getCenterY() - cloud.getRadiusY(),
                cloud.getRadiusX() * 2,
                cloud.getRadiusY() * 2
        );
        drawBorder();
        add(border);
    }

    private void drawBorder(){
        if(showBorder){
            border.setStroke(Color.GREEN);
            border.setFill(Color.TRANSPARENT);
        }
        else{
            border.setStroke(Color.TRANSPARENT);
            border.setFill(Color.TRANSPARENT);
        }
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
            cloud.setFill(color);
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
            cloud.setFill(color);
        }
    }

    public void toggleBoundaries() {
        showBorder = !showBorder;
        drawBorder();
    }

    //Changes this clouds state based on ignition state
    public void changeState(CloudState state){
        this.state = state;
    }

    public void respawn(){
        randomSpawn();
        saturation = 0;
        text.setText(String.format("%.0f %%", saturation));
        cloud.setFill(Color.WHITE);
        this.setTranslateX(spawn.getX());
        this.setTranslateY(spawn.getY());
    }

    @Override
    public void move(){
        state.updateCloud();
    }

    public Rectangle getBorder(){
        return border;
    }

    public double getSpeed(){
        return speed;
    }

    public double getSize(){
        return radius;
    }

    public Point2D getCenter(){
        return new Point2D(cloud.getCenterX() + this.getTranslateX(),
                cloud.getCenterY() + this.getTranslateY());
    }
}

class Pond extends Fixed {
    private double radius;
    private double area;
    private double percentage;
    private final Circle pond;
    private final GameText text;


    public Pond(Point2D cords) {
        super(cords);
        percentage = 20;
        radius = 40;
        area = Math.PI * Math.pow(radius, 2);

        pond = new Circle(radius);
        text = new GameText(String.format("%.0f %%", percentage));
        pond.setFill(Color.BLUE);
        pond.setStroke(Color.BLACK);
        pond.setStrokeWidth(2);
        pond.setCenterX(cords.getX());
        pond.setCenterY(cords.getY());
        text.setFill(Color.WHITE);
        text.setX(cords.getX() - 10);
        text.setY(cords.getY() + 10);

        add(pond);
        add(text);
    }

    public void fillPond(double fillRate) {
        if(percentage < 99){
            percentage += fillRate / 2;
            growPond();
            text.setText(String.format("%.0f %%", percentage));
        }
    }

    private void growPond(){
        pond.setRadius(pond.getRadius() + .2);
    }

    public boolean isFull(){
        return percentage >= 99;
    }

    public Point2D getCenter(){
        return new Point2D(pond.getCenterX() + this.getTranslateX(),
                pond.getCenterY() + this.getTranslateY());
    }
}

class Helipad extends Fixed {
    private Rectangle padSquare;
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
        init();
    }

    private void init(){
        makeHeliImage();
        makeCircle();
        makeRectangle();
    }

    private void makeHeliImage(){
        ImageView padView = new ImageView();

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
        Circle padCircle = new Circle(center.getX(), center.getY(), radius);
        padCircle.setStroke(Color.GRAY);
        padCircle.setStrokeWidth(2);
        padCircle.setFill(Color.TRANSPARENT);
        add(padCircle);
    }

    public static Point2D getCenter(){
        return center;
    }

    public Rectangle padBorder(){
        return padSquare;
    }
}

class HeloBody extends Fixed{
    private Circle rotorCenter;
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
        Circle body = new Circle();
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
        tailRotor.setFill(Color.GREY);
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

//Creates a blade and spins the blade when turned on
class HeloBlade extends GameObject{
    private Rectangle blade;
    private Circle bladeCenter;
    private final Point2D heliCenter;
    private int bladeSpeed;

    HeloBlade(Point2D heliCenter){
        this.heliCenter = heliCenter;
        init();
        spin();
    }

    //Initializes values and creates blade
    private void init(){
        bladeSpeed = 0;
        makeBlade();
        positionPieces();
    }

    //Spins the helicopter blades when turned on
    private void spin(){
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                blade.setRotate(blade.getRotate() + bladeSpeed);
            }
        };
        loop.start();
    }

    //Centers the blade over the correct positions
    private void positionPieces() {
        blade.setX(heliCenter.getX());
        blade.setY(heliCenter.getY());

        blade.setRotate(25);

        bladeCenter.setCenterX(blade.getX() + blade.getWidth() / 2);
        bladeCenter.setCenterY(blade.getY() + blade.getHeight() / 2);
    }

    //Creates the blade object
    private void makeBlade() {
        blade = new Rectangle(5, 200);
        bladeCenter = new Circle(3, Color.BLACK);
        blade.setFill(Color.GREY);
        this.getChildren().addAll(blade, bladeCenter);
    }

    //Updates the blades speed for the blade state
    public void update(int bladeSpeed){
        this.bladeSpeed = bladeSpeed;
    }

}

class Helicopter extends Movable implements Updatable {
    private Rectangle helicopter;
    private GameText fuelText;
    private boolean showBorder;
    private int fuel;
    private double heading = 0;
    private double speed = 0;
    private HeloBlade heliblade;
    private HelicopterState state;
    private int bladeSpeed = 0;
    private final int maxSpeed = 10;

    //Initializes the helicopter object
    public Helicopter(Point2D padCenter, int fuel){
        super();
        this.fuel = fuel;
        showBorder = false;
        state = new Off(this);

        makeHelicopter();
        makeBorder();
        initFuel();

        this.translate(padCenter.getX(), padCenter.getY());
        this.getTransforms().clear();
        this.getTransforms().addAll(myTranslation);
    }

    //Updates the movement and state for the helicopter
    @Override
    public void move() {
        this.getTransforms().clear();
        rotate(heading);

        translate(
                myTranslation.getX()
                        + Math.sin(Math.toRadians(heading)) * -speed,
                myTranslation.getY()
                        + Math.cos(Math.toRadians(heading)) * speed
        );
        this.getTransforms().addAll(myTranslation, myRotation);

        bladeSpeed = state.bladeSpeed(bladeSpeed);
        heliblade.update(bladeSpeed);

        updateFuel();
    }

    //Creates the helicopter all scaled to the game size
    private void makeHelicopter() {
        makeBody();
        makeBlade();
    }

    //Creates, scales, and positions the helicopter body
    private void makeBody(){
        HeloBody helibody = new HeloBody();
        helibody.scale(.5, .5);
        helibody.translate(0, 22);
        add(helibody);
    }

    //Creates, scales, and positions the helicopter blade
    private void makeBlade(){
        heliblade = new HeloBlade(new Point2D(-2.5, -145)); //scale .5
        heliblade.scale(.5, .5);
        heliblade.translate(0, 27);
        add(heliblade);
    }

    private void makeBorder() {
        helicopter = new Rectangle(
                -25, // X
                -52, // Y
                50, // W
                95 // H
        );
        drawBorder();
        add(helicopter);
    }

    private void initFuel(){
        fuelText = new GameText(String.valueOf(fuel));
        fuelText.setX(helicopter.getX());
        fuelText.setY(helicopter.getY() - 10); // move text down 10 for room
        fuelText.setFill(Color.YELLOW);
        add(fuelText);
    }

    //Fuel usage that scales with helicopter speed
    private void updateFuel(){
        if(state instanceof Ready){
            if(Math.abs(speed - 0.0) < 0.001){  //idle fuel
                fuel -= 10;
            }
            else if(speed < maxSpeed / 2){      //helicopter moves slowly
                fuel -= Math.abs(speed * 2);
            }
            else{                               //helicopter moves fast
                fuel -= Math.abs(speed * 5);    // x25 fuel consumption
            }
            fuelText.setText(String.valueOf(fuel));
        }
    }

    //Toggles the boundry box on and off
    public void toggleBoundaries() {
        showBorder = !showBorder;
        drawBorder();
    }

    //Draws the border around the object
    private void drawBorder(){
        if(showBorder){
            helicopter.setStroke(Color.GREEN);
            helicopter.setFill(Color.TRANSPARENT);
        }
        else{
            helicopter.setStroke(Color.TRANSPARENT);
            helicopter.setFill(Color.TRANSPARENT);
        }
    }

    //Moves Left on key-press
    public void rotateLeft(){
        if(state instanceof Ready){
            heading += 15;
        }
    }

    //Moves Right on key-press
    public void rotateRight(){
        if(state instanceof Ready){
            heading -= 15;
        }
    }

    //Moves Foward on key-press
    public void moveForward(){
        if(state instanceof Ready && speed <= maxSpeed){
            speed += .1;
        }
    }

    //Moves Backwards on key-press
    public void moveBackward(){
        int minSpeed = -2;
        if(state instanceof Ready && speed >= minSpeed){
            speed -= .1;
        }
    }

    //Toggles the helicopter ignition on and off
    public void toggleIgnition(){
        state.ignition();
    }

    public int getFuel(){
        return fuel;
    }

    //Changes this helicopters state based on ignition state
    public void changeState(HelicopterState state){
        this.state = state;
    }

    public HelicopterState getState() {
        return state;
    }

    public Rectangle getBorder(){
        return helicopter;
    }

    public Point2D getCenter(){
        /*
        return new Point2D(
                helicopter.getX() + this.getTranslateX(),
                helicopter.getY() + this.getTranslateY()
        );

         */
        return new Point2D(
                ( helicopter.getX() + helicopter.getWidth() / 2 ) + this.getTranslateX(),
                helicopter.getY()
        );
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



class Lines extends GameObject implements Updatable{
    Cloud cloud;
    Pond pond;
    Helicopter helicopter;
    Line line, line2;
    boolean showLine;

    public Lines(Cloud cloud, Pond pond){
        this.cloud = cloud;
        this.pond = pond;
        showLine = false;
        createLine();
    }

    @Override
    public void update(){
        this.getChildren().clear();
        createLine();

        if(showLine){
            line.setStroke(Color.PINK);
        }
        else{
            line.setStroke(Color.TRANSPARENT);
        }
    }

    public void createLine(){
        line = new Line(
                cloud.getCenter().getX(),
                cloud.getCenter().getY(),
                pond.getCenter().getX(),
                pond.getCenter().getY()
        );
        line.setStrokeWidth(2);
        add(line);
    }

    public void toggleVisibility(){
        showLine = !showLine;
    }
}

























//Helicopter States
abstract class HelicopterState {
    static int maxSpeed = 10;
    Helicopter helicopter;

    HelicopterState(Helicopter helicopter){
        this.helicopter = helicopter;
    }

    abstract void ignition();
    abstract int bladeSpeed(int bladeSpeed);
}

class Off extends HelicopterState {
    Off(Helicopter helicopter){
        super(helicopter);
    }

    @Override
    void ignition(){
        helicopter.changeState(new Starting(helicopter));
    }

    @Override
    int bladeSpeed(int bladeSpeed){
        return 0;
    }
}

class Starting extends HelicopterState {
    private int wait = 0;
    Starting(Helicopter heli){
        super(heli);
    }
    @Override
    void ignition(){
        helicopter.changeState(new Stopping(helicopter));
    }

    @Override
    int bladeSpeed(int bladeSpeed){
        wait++;
        if(wait % 50 == 0){
            if(bladeSpeed < maxSpeed){
                bladeSpeed++;
            }
            if(bladeSpeed == maxSpeed){
                helicopter.changeState(new Ready(helicopter));
            }
        }
        return bladeSpeed;
    }
}

class Stopping extends HelicopterState {
    private int wait = 0;
    Stopping(Helicopter heli){
        super(heli);
    }
    @Override
    void ignition(){
        helicopter.changeState(new Starting(helicopter));
    }

    @Override
    int bladeSpeed(int bladeSpeed){
        wait++;
        if(wait % 100 == 0) {
            if (bladeSpeed > 0) {
                bladeSpeed--;
            }
            if (bladeSpeed == 0) {
                helicopter.changeState(new Off(helicopter));
            }
        }
        return 0;
    }
}

class Ready extends HelicopterState {
    Ready(Helicopter heli){
        super(heli);
    }

    @Override
    void ignition(){
        helicopter.changeState(new Stopping(helicopter));
    }

    @Override
    int bladeSpeed(int bladeSpeed){
        return maxSpeed; //returns to max speed
    }
}

//Cloud States
abstract class CloudState {
    Cloud cloud;

    CloudState(Cloud cloud){
        this.cloud = cloud;
    }

    abstract void updateCloud();
}

class CloudAlive extends CloudState {
    public CloudAlive(Cloud cloud) {
        super(cloud);
    }

    @Override
    void updateCloud() {
        cloud.setTranslateX(cloud.getTranslateX() + cloud.getSpeed() * .3);
    }
}

class CloudDead extends CloudState {
    public CloudDead(Cloud cloud) {
        super(cloud);
    }

    @Override
    void updateCloud() {
        cloud.getTransforms().clear();
        cloud.respawn();
        cloud.changeState(new CloudAlive(cloud));
    }
}










