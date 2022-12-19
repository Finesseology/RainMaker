import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;

class Game extends Pane {
    private final ArrayList<Cloud> clouds = new ArrayList<>();
    private final ArrayList<Pond> ponds = new ArrayList<>();
    private final ArrayList<Lines> lines = new ArrayList<>();
    private Helicopter helicopter;
    private Helipad helipad;
    private int frames;
    private final Random rand = new Random();
    private Point2D gameSize;


    Game() {
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
    private void init() {
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
    private void run() {
        updateHelicopter();
        updateClouds();
        updateLines();
        checkWinCondition();
        checkLossCondition();
    }

    //Create a win window
    private void createWinWindow() {
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
    private void checkWinCondition() {
        if (helicopter.getFuel() > 0
                && checkPondsFull()
                && helicopter.getState() instanceof Off
                && !Shape.intersect(helicopter.getBorder(),
                helipad.padBorder()).getBoundsInLocal().isEmpty()) {
            createWinWindow();
            reset();
        }
    }

    //checks to see if all the ponds in the game are full
    private boolean checkPondsFull() {
        int full = 0;
        for (Pond pond : ponds) {
            if (pond.isFull()) {
                full += 1;
            }
        }
        return full == ponds.size();
    }

    //If the helicopter runs out of fuel
    private void checkLossCondition() {
        if (helicopter.getFuel() <= 0) {
            createLossWindow();
            reset();
        }
    }

    //Create a Loss window
    private void createLossWindow() {
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

    //Updates all the cloud movements
    private void updateClouds() {
        for (Cloud cloud : clouds) {
            //if the cloud is offscreen, it is dead
            if (cloud.getTranslateX()
                    > GameApp.windowSize.getX() + cloud.getSize() * 4) {
                cloud.changeState(new CloudDead(cloud));
            }
            //If the cloud can be used, take water from it
            if (helicopter.getState() instanceof Ready
                    && cloud.isNotEmpty() && frames % 150 == 0) {
                cloud.desaturate();
            }
            cloud.move();
        }
    }

    //updates all the helicopter movements
    private void updateHelicopter() {
        helicopter.move(); //updates the helicopter with inputs
    }

    //Updates the lines position/drawing and the distance between them
    private void updateLines() {
        for (Lines line : lines) {
            line.update();
        }
    }

    //Creates Cloud objects and stores them in ArrayList
    private void createClouds() {
        for (int i = 0; i < 4; i++) {
            createCloud();
        }
    }

    private void createCloud() {
        double WIND_SPEED = 1;
        double WIND_DIRECTION = 45;
        Cloud cloud = new Cloud(WIND_SPEED, WIND_DIRECTION);
        clouds.add(cloud);
        getChildren().add(cloud);
        randomSize();
    }

    //Creates Pond object
    private void createPonds() {
        for (int i = 0; i < 3; i++) {
            createPond();
        }
    }

    //Creates Pond objects
    private void createPond() {
        Pond pond = new Pond(gameSize);
        ponds.add(pond);
        getChildren().add(pond);
        randomSize();
    }

    //Creates Helipad Object
    private void createHelipad() {
        helipad = new Helipad();
        getChildren().add(helipad);
    }

    //Creates Helicopter Object
    private void createHelicopter(int fuel) {
        helicopter = new Helicopter(new Point2D(Helipad.getCenter().getX(),
                Helipad.getCenter().getY()), fuel);
        getChildren().add(helicopter);
    }

    //Creates a new line between cloud and pond objects
    private void createLines() {
        for (Cloud cloud : clouds) {
            for (Pond pond : ponds)
                lines.add(new Lines(cloud, pond));
        }
        for (Lines line : lines) {
            getChildren().add(line);
        }
    }

    //Used to create a random gameSize for object placement
    //Creates random point
    private void randomSize() {
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
    public void reset() {
        getChildren().clear();
        clouds.clear();
        ponds.clear();
        lines.clear();
        init();
    }

    //Moves helicopter object to the left on key-press
    public void moveLeft() {
        helicopter.rotateLeft();
    }

    //Moves helicopter object to the right on key-press
    public void moveRight() {
        helicopter.rotateRight();
    }

    //Moves helicopter object forward on key-press
    public void moveForward() {
        helicopter.moveForward();
    }

    //Moves helicopter object backwards on key-press
    public void moveBackward() {
        helicopter.moveBackward();
    }

    //Turns on the helicopter
    public void toggleIgnition() {
        helicopter.toggleIgnition();
    }

    //On 'B' press, shows the boundaries of game objects
    public void showBoundaries() {
        helicopter.toggleBoundaries();
        for (Cloud cloud : clouds) {
            cloud.toggleBoundaries();
        }
    }

    //Toggles the lines between objects on and off
    public void showLines() {
        for (Lines line : lines) {
            line.toggleVisibility();
        }
    }

    //If the helicopter is on and pressing space, decreases cloud
    //saturation and fills the pond
    public void saturateCloud() {
        for (Cloud cloud : clouds) {
            if (overCloud(cloud) && cloud.notFull()) {
                cloud.saturate();
                if (cloud.isReadyToFill()) { //5% fill rate
                    fillPonds();
                }
            }
        }
    }

    //Checks to see if helicopter is over a cloud
    private boolean overCloud(Cloud cloud) {
        return !Shape.intersect(helicopter.getBorder(),
                cloud.getBorder()).getBoundsInLocal().isEmpty();
    }

    //Fills the ponds with water
    private void fillPonds() {
        findShortestLine().pond()
                .fillPond(calculateFillRate(findShortestLine()));
    }

    //Calculates the fillRate based on line length
    private double calculateFillRate(Lines line) {
        double fillRate = 5.0;
        for (Pond pond : ponds) {
            double distance = line.getDistance() / pond.getRadius() * 2;
            if (distance >= 15) {
                fillRate = 0.0;
            } else if (distance >= 10) {
                fillRate *= 0.25;
            } else if (distance >= 5) {
                fillRate *= 0.5;
            } else if (distance >= 3) {
                fillRate *= 0.75;
            }
        }
        return fillRate;
    }

    //Calculates the closest Line to the current cloud
    private Lines findShortestLine() {
        Lines shortest = lines.get(0);
        for (Lines line : lines) {
            if (line.getDistance() < shortest.getDistance()) {
                shortest = line;
            }
        }
        return shortest;
    }

    //Creates a scalable background from a png
    private void makeBackground() {
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
