import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.concurrent.ThreadLocalRandom;

//Creates the pond objects filled with water in the game
class Pond extends Fixed {
    private double percentage;
    private final double radius;
    private final Circle pond;
    private final GameText text;


    public Pond(Point2D cords) {
        super(cords);
        percentage = ThreadLocalRandom.current().nextInt(20, 30);
        radius = percentage * 2;

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
        if (percentage < 100) {
            percentage += fillRate;
            growPond(fillRate);
            text.setText(String.format("%.0f %%", percentage));
        }
    }

    private void growPond(double fillRate) {
        pond.setRadius(pond.getRadius() + fillRate);
    }

    public boolean isFull() {
        return percentage >= 99;
    }

    public Point2D getCenter() {
        return new Point2D(pond.getCenterX() + this.getTranslateX(),
                pond.getCenterY() + this.getTranslateY());
    }

    public double getRadius() {
        return radius;
    }
}
