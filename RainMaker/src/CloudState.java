//Cloud States
abstract class CloudState {
    Cloud cloud;

    CloudState(Cloud cloud) {
        this.cloud = cloud;
    }

    abstract void updateCloud();
}
