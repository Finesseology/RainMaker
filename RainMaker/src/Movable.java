//Classes that are able to move used this abstract class
abstract class Movable extends GameObject {
    double heading;
    double speed;

    public Movable() {
        super();
    }

    public abstract void move();

}
