import javafx.geometry.Point2D;

//Classes that are unable to move used this abstract class
abstract class Fixed extends GameObject {
    public Fixed() {
        super();
    }

    public Fixed(Point2D cords) {
        super(cords);
    }
}
