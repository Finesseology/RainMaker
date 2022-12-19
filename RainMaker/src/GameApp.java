import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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
        stage.setResizable(false);
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

