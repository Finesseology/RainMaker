import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

//Creates text object easily transformable for our game objects
class GameText extends GameObject {
    Text text;

    public GameText(String textString) {
        text = new Text(textString);
        text.setScaleY(-1);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFont(Font.font(20));
        add(text);
        this.getTransforms().addAll(myTranslation, myRotation, myScale);
    }

    public void setFill(Color color) {
        text.setFill(color);
    }

    public void setText(String textString) {
        text.setText(textString);
    }

    public void setX(double x) {
        text.setX(x);
    }

    public void setY(double y) {
        text.setY(y);
    }

}
