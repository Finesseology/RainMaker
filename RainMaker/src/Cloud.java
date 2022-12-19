import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

//Creates the floating intractable clouds in the game
class Cloud extends Movable {
    private double saturation;
    private Color color;
    private double radius;
    private Ellipse cloud;
    private GameText text;
    private boolean showBorder;
    private Rectangle border;
    private CloudState state;
    private final Random rand = new Random();
    private Point2D spawn;

    //Creates the initial cloud
    public Cloud(double speed, double heading) {
        super();
        this.speed = speed;
        this.heading = heading;
        saturation = 0;
        radius = ThreadLocalRandom.current().nextInt(40, 50);
        showBorder = false;
        spawn = new Point2D(0, 0);
        state = new CloudAlive(this);

        randomSpawn();
        initCloud();
        makeBorder();
    }

    private void initCloud() {
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

    private void randomSpawn() {
        int oldY = (int) spawn.getY();
        spawn = new Point2D(
                -radius * 2,
                rand.nextInt(
                        250,
                        500
                )
        );
        if ((spawn.getY() - oldY) < 50) {
            randomSpawn();
        }
    }

    private void randomizeSpeed() {
        speed = ThreadLocalRandom.current().nextDouble(.5, 1.5);
    }

    private void makeBorder() {
        border = new Rectangle(
                cloud.getCenterX() - cloud.getRadiusX(),
                cloud.getCenterY() - cloud.getRadiusY(),
                cloud.getRadiusX() * 2,
                cloud.getRadiusY() * 2
        );
        drawBorder();
        add(border);
    }

    private void drawBorder() {
        if (showBorder) {
            border.setStroke(Color.GREEN);
            border.setFill(Color.TRANSPARENT);
        } else {
            border.setStroke(Color.TRANSPARENT);
            border.setFill(Color.TRANSPARENT);
        }
    }

    public boolean notFull() {
        return saturation < 100;
    }

    public boolean isNotEmpty() {
        return saturation > 0;
    }

    public boolean isReadyToFill() {
        return saturation >= 30;
    }

    public void saturate() {
        if (saturation < 100) {
            saturation += 1;
            text.setText(String.format("%.0f %%", saturation));
            color = Color.rgb(
                    (int) (255 * color.getRed() - 1),
                    (int) (255 * color.getGreen() - 1),
                    (int) (255 * color.getBlue() - 1)
            );
            cloud.setFill(color);
        }
    }

    public void desaturate() {
        if (saturation > 0) {
            saturation -= 1;
            text.setText(String.format("%.0f %%", saturation));
            color = Color.rgb(
                    (int) (255 * color.getRed() + 1),
                    (int) (255 * color.getGreen() + 1),
                    (int) (255 * color.getBlue() + 1)
            );
            cloud.setFill(color);
        }
    }

    public void toggleBoundaries() {
        showBorder = !showBorder;
        drawBorder();
    }

    //Changes this clouds state based on ignition state
    public void changeState(CloudState state) {
        this.state = state;
    }

    //when clouds die, respawn resets them
    public void respawn() {
        randomSpawn();
        randomizeSpeed();
        radius = ThreadLocalRandom.current().nextInt(40, 60);
        color = Color.rgb(255, 255, 255);
        saturation = 0;
        text.setText(String.format("%.0f %%", saturation));
        cloud.setFill(Color.WHITE);
        this.setTranslateX(spawn.getX());
        this.setTranslateY(spawn.getY());
    }

    @Override
    public void move() {
        state.updateCloud();
    }

    public Rectangle getBorder() {
        return border;
    }

    public double getSpeed() {
        return speed;
    }

    public double getSize() {
        return radius;
    }

    public Point2D getCenter() {
        return new Point2D(cloud.getCenterX() + this.getTranslateX(),
                cloud.getCenterY() + this.getTranslateY());
    }
}
