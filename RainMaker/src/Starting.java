//Helicopter State for spinning up the rotors
class Starting extends HelicopterState {
    private int wait = 0;

    Starting(Helicopter heli) {
        super(heli);
    }

    @Override
    void ignition() {
        helicopter.changeState(new Stopping(helicopter));
    }

    @Override
    int bladeSpeed(int bladeSpeed) {
        wait++;
        if (wait % 40 == 0) {
            if (bladeSpeed < maxSpeed) {
                bladeSpeed++;
            }
            if (bladeSpeed == maxSpeed) {
                helicopter.changeState(new Ready(helicopter));
            }
        }
        return bladeSpeed;
    }
}
