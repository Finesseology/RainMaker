import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

//Creates a blade and spins the blade when turned on
class HeloBlade extends GameObject {
    private Rectangle blade;
    private Circle bladeCenter;
    private final Point2D heliCenter;
    private int bladeSpeed;

    HeloBlade(Point2D heliCenter) {
        this.heliCenter = heliCenter;
        init();
        spin();
    }

    //Initializes values and creates blade
    private void init() {
        bladeSpeed = 0;
        makeBlade();
        positionPieces();
    }

    //Spins the helicopter blades when turned on
    private void spin() {
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
    public void update(int bladeSpeed) {
        this.bladeSpeed = bladeSpeed;
    }

}
