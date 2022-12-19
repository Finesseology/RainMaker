import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

//The helicopter object used as the main interactive piece for the game
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
    public Helicopter(Point2D padCenter, int fuel) {
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
    private void makeBody() {
        HeloBody helibody = new HeloBody();
        helibody.scale(.5, .5);
        helibody.translate(0, 22);
        add(helibody);
    }

    //Creates, scales, and positions the helicopter blade
    private void makeBlade() {
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

    private void initFuel() {
        fuelText = new GameText(String.valueOf(fuel));
        fuelText.setX(helicopter.getX());
        fuelText.setY(helicopter.getY() - 10); // move text down 10 for room
        fuelText.setFill(Color.YELLOW);
        add(fuelText);
    }

    //Fuel usage that scales with helicopter speed
    private void updateFuel() {
        if (state instanceof Ready) {
            if (Math.abs(speed - 0.0) < 0.001) {  //idle fuel
                fuel -= 10;
            } else if (speed < maxSpeed / 2.0) {      //helicopter moves slowly
                fuel -= Math.abs(speed * 2);
            } else {                               //helicopter moves fast
                fuel -= Math.abs(speed * 5);    // x25 fuel consumption
            }
            fuelText.setText(String.valueOf(fuel));
        }
    }

    //Toggles the boundary box on and off
    public void toggleBoundaries() {
        showBorder = !showBorder;
        drawBorder();
    }

    //Draws the border around the object
    private void drawBorder() {
        if (showBorder) {
            helicopter.setStroke(Color.GREEN);
            helicopter.setFill(Color.TRANSPARENT);
        } else {
            helicopter.setStroke(Color.TRANSPARENT);
            helicopter.setFill(Color.TRANSPARENT);
        }
    }

    //Moves Left on key-press
    public void rotateLeft() {
        if (state instanceof Ready) {
            heading += 15;
        }
    }

    //Moves Right on key-press
    public void rotateRight() {
        if (state instanceof Ready) {
            heading -= 15;
        }
    }

    //Moves Foward on key-press
    public void moveForward() {
        if (state instanceof Ready && speed <= maxSpeed) {
            speed += .1;
        }
    }

    //Moves Backwards on key-press
    public void moveBackward() {
        int minSpeed = -2;
        if (state instanceof Ready && speed >= minSpeed) {
            speed -= .1;
        }
    }

    //Toggles the helicopter ignition on and off
    public void toggleIgnition() {
        state.ignition();
    }

    public int getFuel() {
        return fuel;
    }

    //Changes this helicopters state based on ignition state
    public void changeState(HelicopterState state) {
        this.state = state;
    }

    public HelicopterState getState() {
        return state;
    }

    public Rectangle getBorder() {
        return helicopter;
    }

}
