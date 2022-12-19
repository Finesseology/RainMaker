//Helicopter State for ignition off
class Off extends HelicopterState {
    Off(Helicopter helicopter) {
        super(helicopter);
    }

    @Override
    void ignition() {
        helicopter.changeState(new Starting(helicopter));
    }

    @Override
    int bladeSpeed(int bladeSpeed) {
        return 0;
    }
}
