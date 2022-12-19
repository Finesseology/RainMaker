//Helicopter States
abstract class HelicopterState {
    static int maxSpeed = 10;
    Helicopter helicopter;

    HelicopterState(Helicopter helicopter) {
        this.helicopter = helicopter;
    }

    abstract void ignition();

    abstract int bladeSpeed(int bladeSpeed);
}
