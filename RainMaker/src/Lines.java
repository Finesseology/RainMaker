import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

//Creates and deals with the lines of the objects on screen
class Lines extends GameObject implements Updatable {
    private final Cloud cloud;
    private final Pond pond;
    private Line line;
    private GameText distanceText;
    private boolean showLine;
    private double distance;

    public Lines(Cloud cloud, Pond pond) {
        this.cloud = cloud;
        this.pond = pond;
        distance = 0;
        showLine = false;
        createLine();
        createText();
    }

    @Override
    public void update() {
        this.getChildren().clear();
        createLine();
        calculateDistance();
        createText();
        if (showLine) {
            line.setStroke(Color.PINK);
            distanceText.setFill(Color.DEEPPINK);
        } else {
            line.setStroke(Color.TRANSPARENT);
            distanceText.setFill(Color.TRANSPARENT);
        }
    }

    //Creates the lines to be drawn between clouds and ponds
    private void createLine() {
        line = new Line(
                cloud.getCenter().getX(),
                cloud.getCenter().getY(),
                pond.getCenter().getX(),
                pond.getCenter().getY()
        );
        line.setStrokeWidth(2);
        add(line);
    }

    //Calculates the distance between current pond and cloud
    private void calculateDistance() {
        distance = Math.hypot(
                cloud.getCenter().getX() - pond.getCenter().getX(),
                cloud.getCenter().getY() - pond.getCenter().getY()
        );
    }

    //Creates the text to be placed on the lines to display distance
    private void createText() {
        distanceText = new GameText(String.format("%.0f ", distance));
        distanceText.setX(line.getBoundsInLocal().getCenterX() - 10);
        distanceText.setY(line.getBoundsInLocal().getCenterY() - 10);
        add(distanceText);
    }

    //Toggles whether or not the lines should be visible
    public void toggleVisibility() {
        showLine = !showLine;
    }

    //Used to find the distance of the current line.
    public double getDistance() {
        return distance;
    }

    public Pond pond() {
        return pond;
    }
}
