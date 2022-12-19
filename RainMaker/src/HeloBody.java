import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

//Creates the helicopter body graphics
class HeloBody extends Fixed {
    private Circle rotorCenter;
    private Rectangle rotorMain;
    private Rectangle leftSkid, rightSkid;
    private Rectangle leftSkidConnectorFront, leftSkidConnectorBack,
            rightSkidConnectorFront, rightSkidConnectorBack;
    private Rectangle tail;
    private Rectangle tailRotor, tailRotorConnector;
    private Color paint;

    HeloBody() {
        init();
    }

    private void init() {
        paint = Color.ORANGE;
        makeBody();
        makeRotorMain();
        makeTail();
        makeSkids();
        makeTailRotor();
        makeWindow();
        positionPieces();
    }

    private void positionPieces() {
        positionMainBody();
        positionSkids();
        positionRotor();
    }

    private void positionMainBody() {
        rotorMain.setX(-40);
        rotorMain.setY(-50);

        rotorCenter.setCenterX(rotorMain.getX() + rotorMain.getWidth() / 2);
        rotorCenter.setCenterY(rotorMain.getY() + rotorMain.getHeight() / 2);

        tail.setX(-15);
        tail.setY(-140);
    }

    private void positionSkids() {
        leftSkid.setX(-50);
        leftSkid.setY(-65);

        rightSkid.setX(45);
        rightSkid.setY(-65);

        leftSkidConnectorFront.setX(-50);
        leftSkidConnectorFront.setY(0);

        leftSkidConnectorBack.setX(-50);
        leftSkidConnectorBack.setY(-37);

        rightSkidConnectorFront.setX(35);
        rightSkidConnectorFront.setY(0);

        rightSkidConnectorBack.setX(35);
        rightSkidConnectorBack.setY(-37);
    }

    private void positionRotor() {
        tailRotor.setX(20);
        tailRotor.setY(-150);
        tailRotorConnector.setX(5);
        tailRotorConnector.setY(-132.5);
    }

    private void makeBody() {
        Circle body = new Circle();
        body.setRadius(40);
        body.setFill(paint);
        add(body);
    }

    private void makeRotorMain() {
        rotorMain = new Rectangle(80, 30);
        rotorCenter = new Circle(5);

        rotorMain.setFill(paint);
        rotorCenter.setFill(Color.GREY);

        add(rotorMain);
        add(rotorCenter);
    }

    private void makeTail() {
        tail = new Rectangle(30, 90);
        tail.setFill(paint);
        add(tail);
    }

    private void makeSkids() {
        double skidHeight = 90, skidWidth = 5;
        double skidConnHeight = 5, skidConnWidth = 15;

        leftSkid = new Rectangle(skidWidth, skidHeight);
        rightSkid = new Rectangle(skidWidth, skidHeight);
        leftSkid.setFill(paint);
        rightSkid.setFill(paint);

        leftSkidConnectorFront = new Rectangle(skidConnWidth, skidConnHeight);
        leftSkidConnectorBack = new Rectangle(skidConnWidth, skidConnHeight);
        rightSkidConnectorFront = new Rectangle(skidConnWidth, skidConnHeight);
        rightSkidConnectorBack = new Rectangle(skidConnWidth, skidConnHeight);
        leftSkidConnectorFront.setFill(paint);
        leftSkidConnectorBack.setFill(paint);
        rightSkidConnectorFront.setFill(paint);
        rightSkidConnectorBack.setFill(paint);

        add(leftSkid);
        add(rightSkid);
        add(leftSkidConnectorFront);
        add(leftSkidConnectorBack);
        add(rightSkidConnectorFront);
        add(rightSkidConnectorBack);
    }

    private void makeTailRotor() {
        tailRotor = new Rectangle(5, 40);
        tailRotorConnector = new Rectangle(20, 5);
        tailRotor.setFill(Color.GREY);
        tailRotorConnector.setFill(paint);
        add(tailRotor);
        add(tailRotorConnector);
    }

    private void makeWindow() {
        Arc window = new Arc(0, 8, 32, 30, 180, 180);
        window.setFill(Color.BLUE);
        add(window);
    }


}
