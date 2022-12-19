//Cloud State for being offscreen
class CloudDead extends CloudState {
    public CloudDead(Cloud cloud) {
        super(cloud);
    }

    @Override
    void updateCloud() {
        cloud.getTransforms().clear();
        cloud.respawn();
        cloud.changeState(new CloudAlive(cloud));
    }
}
