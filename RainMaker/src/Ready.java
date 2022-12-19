//Helicopter State for being ready to fly
class Ready extends HelicopterState {
    Ready(Helicopter heli) {
        super(heli);
    }

    @Override
    void ignition() {
        helicopter.changeState(new Stopping(helicopter));
    }

    @Override
    int bladeSpeed(int bladeSpeed) {
        return maxSpeed; //returns to max speed
    }
}
