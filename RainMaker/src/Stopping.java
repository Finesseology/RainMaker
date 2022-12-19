//Helicopter State for slowing down the rotors
class Stopping extends HelicopterState {
    private int wait = 0;

    Stopping(Helicopter heli) {
        super(heli);
    }

    @Override
    void ignition() {
        helicopter.changeState(new Starting(helicopter));
    }

    @Override
    int bladeSpeed(int bladeSpeed) {
        wait++;
        if (wait % 100 == 0) {
            if (bladeSpeed > 0) {
                bladeSpeed--;
            }
            if (bladeSpeed == 0) {
                helicopter.changeState(new Off(helicopter));
            }
        }
        return 0;
    }
}
